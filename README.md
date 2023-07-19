# XPainterClient
A quick client for XPainter

## Usage
The client currently can only fill a particular area with a particular block.

First select a region with `^pos1` and `^pos2`. You only have to look at the desired block when running that.
You should now see an outline highlighting your region (it may be hard to see).

Next, select your desired block for your brush like normal in XPainter by left-clicking and opening the menu, then enter the name of the block you just selected in `^fill <block>`.

It should now start painting the region (will not attempt to paint if ink is below 10).

When you want to stop, run `^stop`. You can also use `^desel` to clear your region selection.

There are no fancy features like automatic brush selection/detection so you have to do all your block selections manually.
