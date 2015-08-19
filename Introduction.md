# Introduction #

The spatiotemporal-accessibility-toolkit aims to allow the dynamic creation of potential path areas and to allow compatibility with existing Java open source tools. The library is therefore fully compatible with the popular GeoTools library (version 2.6). The source code includes an example Java class (net.sta.toolkit.testexample.Main.java) to demonstrate how to use the toolkit and a trial road dataset can be found under the downloads tab along with a pre-compiled jar file to drop straight into an existing project.

# Functionalities #

  * create equal ranges of potential path areas (e.g. one area for every ten minutes up to a chosen time limit) **OR** discrete ranges for single or multiple time values (e.g. two areas for 12 and 44 minutes).
  * Create both rings or disk areas
  * Output either as contours (polylines) or as areas (polygons)

# Requirements #

  * Topologically correct network dataset with one attribute field (this field must be named _SpeedLimit_) for each network element representing the speeds in Kmph of the movement across it
  * recommended that your system has at least 2GB RAM (for large network datasets)

# How it works #

  * Calculate spatio-temporal distance from origin to all network nodes using Dijkstra Algorithm (using GeoTools graph library)
  * Build a triangulation of these nodes
  * contour the triangulation using the values supplied by the user as the breaks
  * Polygonize (using Sextante) the contours if required to produce areas
  * Output is in the form of a GeoTools FeatureClass which contains either the contours or areas with each object also possessing a time attribute.
  * Times to create these areas depends on number of unique break value and the speed of the system available, as a guide the three images below took 56 seconds, 7 seconds and 5 seconds respectively to produce on a system with 3.3Ghz and 8GB RAM.



**Image Below : ranges for every 10 minutes up to a limit of 60 minutes for Zürich, Switzerland**

<img src='https://lh4.googleusercontent.com/-1CMeMImCnWM/Tl3dBKJacaI/AAAAAAAAA7U/ZK6rgsYLUxU/s576/PPA_Example.png' border='5' />


**Image Below : discrete values for 10 minute and 35 minute polygons for Zürich, Switzerland**

<img src='https://lh5.googleusercontent.com/-gDJ6niF2Xjg/Tl3dBCW7LII/AAAAAAAAA7Y/eYzLFDjOxnk/s576/PPA_Example_discrete.png ' border='5' />

**Image Below : discrete values represented as contours for 10 minutes and 35 minutes for Zürich, Switzerland**

<img src='https://lh4.googleusercontent.com/-3Pc-7oW_9AE/Tl3dBKde5_I/AAAAAAAAA7Q/LzbvfL4Hrfk/s576/PPA_Example_discreteContour.png' border='5' />