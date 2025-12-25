const express = require('express');
const cors = require('cors');
const admin = require('firebase-admin');
const https = require('https');

function initFirebase() {
  if (admin.apps.length) return;

  const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  if (!raw) {
    throw new Error('Missing FIREBASE_SERVICE_ACCOUNT_JSON');
  }

  let serviceAccount;
  try {
    serviceAccount = JSON.parse(raw);
  } catch (e) {
    throw new Error('FIREBASE_SERVICE_ACCOUNT_JSON must be valid JSON');
  }

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
}

function haversineKm(aLat, aLon, bLat, bLon) {
  const R = 6371;
  const dLat = ((bLat - aLat) * Math.PI) / 180;
  const dLon = ((bLon - aLon) * Math.PI) / 180;
  const lat1 = (aLat * Math.PI) / 180;
  const lat2 = (bLat * Math.PI) / 180;

  const sinDLat = Math.sin(dLat / 2);
  const sinDLon = Math.sin(dLon / 2);

  const h = sinDLat * sinDLat + Math.cos(lat1) * Math.cos(lat2) * sinDLon * sinDLon;
  return 2 * R * Math.asin(Math.sqrt(h));
}

function chunkArray(arr, size) {
  const out = [];
  for (let i = 0; i < arr.length; i += size) {
    out.push(arr.slice(i, i + size));
  }
  return out;
}

function jpushRequest(path, payload) {
  const appKey = process.env.JPUSH_APP_KEY;
  const masterSecret = process.env.JPUSH_MASTER_SECRET;
  if (!appKey || !masterSecret) {
    throw new Error('Missing JPUSH_APP_KEY or JPUSH_MASTER_SECRET');
  }

  const auth = Buffer.from(`${appKey}:${masterSecret}`).toString('base64');
  const body = JSON.stringify(payload);

  return new Promise((resolve, reject) => {
    const req = https.request(
      {
        method: 'POST',
        hostname: 'api.jpush.cn',
        path,
        headers: {
          Authorization: `Basic ${auth}`,
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(body),
        },
        timeout: 8000,
      },
      (res) => {
        let data = '';
        res.setEncoding('utf8');
        res.on('data', (c) => (data += c));
        res.on('end', () => {
          const ok = res.statusCode >= 200 && res.statusCode < 300;
          if (ok) return resolve({ status: res.statusCode, body: data });
          return reject(new Error(`JPush error ${res.statusCode}: ${data}`));
        });
      }
    );

    req.on('timeout', () => {
      req.destroy(new Error('JPush request timeout'));
    });
    req.on('error', (e) => reject(e));
    req.write(body);
    req.end();
  });
}

async function sendJPushToRegistrationIds(registrationIds, extras, title, alert) {
  const ids = (registrationIds || []).filter((x) => typeof x === 'string' && x.trim()).map((x) => x.trim());
  if (!ids.length) return { ok: true, sent: 0 };

  const batches = chunkArray(ids, 1000);
  for (const batch of batches) {
    await jpushRequest('/v3/push', {
      platform: 'android',
      audience: { registration_id: batch },
      notification: {
        android: {
          alert: String(alert || ''),
          title: String(title || ''),
          extras: extras || {},
        },
      },
      options: {
        time_to_live: 60,
      },
    });
  }

  return { ok: true, sent: ids.length };
}

const app = express();
app.use(cors());
app.use(express.json({ limit: '1mb' }));

app.get('/', (req, res) => {
  res.json({ ok: true, message: 'HarmonyCare API', health: '/api/health' });
});

app.get('/api/health', (req, res) => {
  res.json({ ok: true });
});

app.post('/api/devices/register', async (req, res) => {
  try {
    initFirebase();
    const db = admin.firestore();

    const {
      user_id,
      role,
      jpush_id,
      fcm_token,
      is_available,
      latitude,
      longitude,
    } = req.body || {};

    const pushId = typeof jpush_id === 'string' && jpush_id.trim() ? jpush_id.trim() : (typeof fcm_token === 'string' ? fcm_token.trim() : '');

    if (!user_id || !role || !pushId) {
      return res.status(400).json({ error: 'user_id, role, jpush_id are required' });
    }

    const docId = `${role}_${user_id}`;
    const now = Date.now();

    await db.collection('devices').doc(docId).set(
      {
        userId: Number(user_id),
        role: String(role),
        jpushId: String(pushId),
        isAvailable: Boolean(is_available),
        latitude: typeof latitude === 'number' ? latitude : null,
        longitude: typeof longitude === 'number' ? longitude : null,
        lastSeenAt: now,
        updatedAt: now,
      },
      { merge: true }
    );

    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ error: e.message || 'Server error' });
  }
});

app.post('/api/volunteers/availability', async (req, res) => {
  try {
    initFirebase();
    const db = admin.firestore();

    const { volunteer_id, is_available, latitude, longitude, jpush_id, fcm_token } = req.body || {};
    if (!volunteer_id) {
      return res.status(400).json({ error: 'volunteer_id is required' });
    }

    const docId = `volunteer_${volunteer_id}`;
    const now = Date.now();

    const patch = {
      userId: Number(volunteer_id),
      role: 'volunteer',
      isAvailable: Boolean(is_available),
      lastSeenAt: now,
      updatedAt: now,
    };

    if (typeof latitude === 'number') patch.latitude = latitude;
    if (typeof longitude === 'number') patch.longitude = longitude;
    const pushId = typeof jpush_id === 'string' && jpush_id.trim() ? jpush_id.trim() : (typeof fcm_token === 'string' ? fcm_token.trim() : '');
    if (pushId) patch.jpushId = pushId;

    await db.collection('devices').doc(docId).set(patch, { merge: true });

    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ error: e.message || 'Server error' });
  }
});

app.post('/api/emergencies', async (req, res) => {
  try {
    initFirebase();
    const db = admin.firestore();

    const { elderly_id, latitude, longitude, timestamp, status } = req.body || {};
    if (!elderly_id || typeof latitude !== 'number' || typeof longitude !== 'number') {
      return res.status(400).json({ error: 'elderly_id, latitude, longitude are required' });
    }

    const emergencyId = Number(timestamp) && Number(timestamp) > 0 ? Number(timestamp) : Date.now();
    const now = Date.now();

    const emergencyDoc = {
      id: emergencyId,
      elderlyId: Number(elderly_id),
      latitude,
      longitude,
      status: String(status || 'active'),
      volunteerId: null,
      createdAt: now,
      updatedAt: now,
    };

    await db.collection('emergencies').doc(String(emergencyId)).set(emergencyDoc, { merge: false });

    await db.collection('audit_logs').add({
      emergencyId,
      action: 'created',
      actorRole: 'elderly',
      actorUserId: Number(elderly_id),
      createdAt: now,
    });

    const devicesSnap = await db
      .collection('devices')
      .where('role', '==', 'volunteer')
      .where('isAvailable', '==', true)
      .limit(200)
      .get();

    const volunteers = [];
    devicesSnap.forEach((doc) => {
      const d = doc.data();
      const lastSeenAt = typeof d.lastSeenAt === 'number' ? d.lastSeenAt : 0;
      const maxAgeMs = 10 * 60 * 1000;
      if (!d.jpushId) return;
      if (now - lastSeenAt > maxAgeMs) return;

      let distanceKm = null;
      if (typeof d.latitude === 'number' && typeof d.longitude === 'number') {
        distanceKm = haversineKm(latitude, longitude, d.latitude, d.longitude);
      }

      volunteers.push({
        userId: d.userId,
        jpushId: d.jpushId,
        distanceKm,
      });
    });

    volunteers.sort((a, b) => {
      if (a.distanceKm == null && b.distanceKm == null) return 0;
      if (a.distanceKm == null) return 1;
      if (b.distanceKm == null) return -1;
      return a.distanceKm - b.distanceKm;
    });

    const tokens = volunteers.map((v) => v.jpushId).filter(Boolean);
    const notifiedIds = volunteers.map((v) => v.userId).filter((x) => typeof x === 'number');

    if (tokens.length) {
      try {
        await sendJPushToRegistrationIds(
          tokens,
          {
            type: 'emergency_new',
            emergency_id: String(emergencyId),
            elderly_id: String(elderly_id),
            latitude: String(latitude),
            longitude: String(longitude),
          },
          'HarmonyCare SOS',
          'New Emergency Request'
        );
      } catch (pushErr) {
        await db.collection('audit_logs').add({
          emergencyId,
          action: 'push_failed_to_volunteers',
          actorRole: 'system',
          actorUserId: null,
          error: String(pushErr && pushErr.message ? pushErr.message : pushErr),
          createdAt: Date.now(),
        });
      }

      await db.collection('audit_logs').add({
        emergencyId,
        action: 'pushed_to_volunteers',
        actorRole: 'system',
        actorUserId: null,
        notifiedVolunteerUserIds: notifiedIds,
        createdAt: Date.now(),
      });
    }

    res.status(201).json({ id: emergencyId });
  } catch (e) {
    res.status(500).json({ error: e.message || 'Server error' });
  }
});

app.get('/api/emergencies/active', async (req, res) => {
  try {
    initFirebase();
    const db = admin.firestore();

    const volunteerId = req.query.volunteer_id ? Number(req.query.volunteer_id) : null;

    const activeSnap = await db.collection('emergencies').where('status', '==', 'active').limit(200).get();
    const emergencies = [];
    activeSnap.forEach((doc) => emergencies.push(doc.data()));

    if (volunteerId) {
      const acceptedSnap = await db
        .collection('emergencies')
        .where('status', '==', 'accepted')
        .where('volunteerId', '==', volunteerId)
        .limit(200)
        .get();
      acceptedSnap.forEach((doc) => emergencies.push(doc.data()));
    }

    const out = emergencies.map((e) => ({
      id: Number(e.id),
      elderly_id: Number(e.elderlyId),
      latitude: Number(e.latitude),
      longitude: Number(e.longitude),
      timestamp: Number(e.createdAt || e.id),
      status: String(e.status),
      volunteer_id: e.volunteerId == null ? null : Number(e.volunteerId),
    }));

    res.json(out);
  } catch (e) {
    res.status(500).json({ error: e.message || 'Server error' });
  }
});

app.put('/api/emergencies/:id', async (req, res) => {
  try {
    initFirebase();
    const db = admin.firestore();

    const emergencyId = Number(req.params.id);
    if (!emergencyId) {
      return res.status(400).json({ error: 'Invalid emergency id' });
    }

    const { status, volunteer_id } = req.body || {};
    if (!status) {
      return res.status(400).json({ error: 'status is required' });
    }

    const ref = db.collection('emergencies').doc(String(emergencyId));

    if (String(status) === 'accepted') {
      if (!volunteer_id) {
        return res.status(400).json({ error: 'volunteer_id is required for accepted' });
      }

      try {
        await db.runTransaction(async (tx) => {
          const snap = await tx.get(ref);
          if (!snap.exists) {
            throw new Error('NOT_FOUND');
          }
          const current = snap.data();
          if (current.status !== 'active') {
            const err = new Error('ALREADY_TAKEN');
            err.code = 'ALREADY_TAKEN';
            throw err;
          }

          tx.update(ref, {
            status: 'accepted',
            volunteerId: Number(volunteer_id),
            acceptedAt: Date.now(),
            updatedAt: Date.now(),
          });
        });
      } catch (err) {
        if (err.message === 'NOT_FOUND') {
          return res.status(404).json({ error: 'Emergency not found' });
        }
        if (err.code === 'ALREADY_TAKEN' || err.message === 'ALREADY_TAKEN') {
          return res.status(409).json({ error: 'Emergency already accepted' });
        }
        throw err;
      }

      await db.collection('audit_logs').add({
        emergencyId,
        action: 'accepted',
        actorRole: 'volunteer',
        actorUserId: Number(volunteer_id),
        createdAt: Date.now(),
      });

      const emergency = (await ref.get()).data();
      if (emergency && emergency.elderlyId != null) {
        const elderlyDevice = await db.collection('devices').doc(`elderly_${emergency.elderlyId}`).get();
        const elderlyPushId = elderlyDevice.exists ? elderlyDevice.data().jpushId : null;
        if (elderlyPushId) {
          try {
            await sendJPushToRegistrationIds(
              [String(elderlyPushId)],
              {
                type: 'emergency_accepted',
                emergency_id: String(emergencyId),
                volunteer_id: String(volunteer_id),
              },
              'HarmonyCare SOS',
              'Emergency Accepted'
            );
          } catch (pushErr) {
            await db.collection('audit_logs').add({
              emergencyId,
              action: 'push_failed_to_elderly',
              actorRole: 'system',
              actorUserId: null,
              error: String(pushErr && pushErr.message ? pushErr.message : pushErr),
              createdAt: Date.now(),
            });
          }
        }
      }

      return res.json({ ok: true });
    }

    const patch = {
      status: String(status),
      updatedAt: Date.now(),
    };

    if (typeof volunteer_id !== 'undefined') {
      patch.volunteerId = volunteer_id == null ? null : Number(volunteer_id);
    }

    const snap = await ref.get();
    if (!snap.exists) {
      return res.status(404).json({ error: 'Emergency not found' });
    }

    await ref.update(patch);

    await db.collection('audit_logs').add({
      emergencyId,
      action: `status_${String(status)}`,
      actorRole: 'system',
      actorUserId: null,
      createdAt: Date.now(),
    });

    res.json({ ok: true });
  } catch (e) {
    res.status(500).json({ error: e.message || 'Server error' });
  }
});

app.post('/api/admin/cleanup', async (req, res) => {
  try {
    initFirebase();
    const db = admin.firestore();

    const token = req.headers['x-admin-token'];
    if (!process.env.ADMIN_TOKEN || token !== process.env.ADMIN_TOKEN) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    const days = req.query.days ? Number(req.query.days) : (process.env.RETENTION_DAYS ? Number(process.env.RETENTION_DAYS) : 30);
    const cutoff = Date.now() - days * 24 * 60 * 60 * 1000;

    const snap = await db.collection('emergencies').where('createdAt', '<', cutoff).limit(200).get();
    const batch = db.batch();
    let count = 0;
    snap.forEach((doc) => {
      batch.delete(doc.ref);
      count += 1;
    });

    if (count) {
      await batch.commit();
    }

    res.json({ ok: true, deleted: count, days });
  } catch (e) {
    res.status(500).json({ error: e.message || 'Server error' });
  }
});

module.exports = app;
