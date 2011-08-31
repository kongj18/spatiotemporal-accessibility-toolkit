---------------------------------------------------
Instructions for installing SEXTANTE for gvSIG 
---------------------------------------------------

- Locate the folder where you have installed gvSIG (version 1.9 is needed). Under Windows, it is usually something like "C:\Program Files\gvSIG_1_9
- Locate the extensions folder at [gvSIG folder]\bin\gvSIG\extensiones
- Under the extensions folder, create a new folder named es.unex.sextante under it
- copy the gvSIG binding files to that folder
- copy the SEXTANTE core files to that folder, except jts-1.9.jar (this causes conflicts with the JTS library already included in gvSIG)
- delete the file jts-1.9.jar (this causes conflicts with the JTS library already included in gvSIG)

