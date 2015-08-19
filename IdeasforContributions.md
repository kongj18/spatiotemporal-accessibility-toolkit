Main Ideas -

1) Speed Up Accessible Area Creation -

instead of the all this triangulation and polygon building, simply build the network and the loop through it. For each node that takes less time to reach than the total time budget, create a buffer the size of the distance the user would be able to walk in the time left over.

e.g.
<p>TimeBudget=20 minutes</p>
<p>FilteredNodes=Filter out all points over 20minutes away</p>
<p>for all FilteredNodes{</p>
<p>Node 1 = 10 minutes to reach;</p>
<p>timeLeftOver=10 minutes;</p>
<p>WalkSpeed=5Kmperhour;</p>
<p>bufferSize = 5Kmperhour*10mins/60mins = 0.83km</p>
<p>ApplyBufferToNodePoint</p>
<p>}</p>

then union the buffers using JTS.

To make this faster create only a raster image using the BaTIK toolkit. create SVG circles instead of the buffers, with the r attribute based on the bufferSize result above.