# AMap SDK Files

এই folder-এ AMap SDK files রাখুন:

## Required Files:
1. **3D Map SDK**: 
   - `AMap_3DMap_Vx.x.x_xxx.jar` বা `.aar` file
   - বা `3dmap` folder (যদি multiple files থাকে)

2. **Navigation SDK**:
   - `AMap_Navi_Vx.x.x_xxx.jar` বা `.aar` file
   - বা `navi` folder (যদি multiple files থাকে)

3. **Location SDK** (optional, কিন্তু recommended):
   - `AMap_Location_Vx.x.x_xxx.jar` বা `.aar` file

4. **Search SDK** (optional):
   - `AMap_Search_Vx.x.x_xxx.jar` বা `.aar` file

## Instructions:
1. AMap Developer Console থেকে SDK download করুন: https://developer.amap.com/api/android/download
2. SDK files extract করুন
3. সব `.jar` এবং `.aar` files এই `libs` folder-এ copy করুন
4. Developer কে জানান যে files ready হয়েছে

## Note:
- যদি SDK multiple `.jar` files থাকে, সব files এই folder-এ রাখুন
- `.so` files (native libraries) থাকলে `app/src/main/jniLibs/` folder-এ রাখতে হবে

