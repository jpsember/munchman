package vgpackage;
import java.awt.*;
import java.awt.image.PixelGrabber;

import mytools.*;

public class SpriteFinder {
    private SpriteFinder() {}

    /**
     * Constructs a SpriteFinder object, based upon an existing sprite.
     * @param s sprite to search for nested sprites
     */
    public SpriteFinder(Sprite s) {
        sprite = s;

        grabPixels();
        frameCount = 0;
        workFrames = new int[MAX_SPRITES * WORKFRAMELEN];
        bgColor = pix[0];
        findSprites();

        // Clear references to these arrays that were only used during
        // construction.
        pix = null;
        workFrames = null;
    }

    public Sprite find(int n) {
        int j = n * SPRITERECLEN;
        return new Sprite(sprite,
            spriteRecs[j+0],spriteRecs[j+1],
            spriteRecs[j+2],spriteRecs[j+3],
            spriteRecs[j+4],spriteRecs[j+5] );
    }

    // Find the next stretch of pixels that is not within an existing frame.
    private int skipExistingFrames(Point pt) {
        int smallestRowSize = 0;
        while (true) {
            if (pt.x == sprite.w) {
                pt.x = 0;
                pt.y++;
                if (pt.y == sprite.h)
                    break;      // done the sprite.
            }

            // Examine list of frames.
            smallestRowSize = sprite.w - pt.x;
            int j = 0;
            for (int i = 0; i < frameCount; i++, j += WORKFRAMELEN) {
                // We can assume that no frames exist that are entirely
                // below our scan location, since we haven't scanned down
                // there yet!

                // If this frame is entirely above our scan location,
                // skip further tests.
                if (workFrames[j+5] <= pt.y) continue;

                // Is frame entirely to the left?
                int rightEdge = workFrames[j+4];

                if (rightEdge <= pt.x) continue;

                int rowSize = workFrames[j+0] - pt.x;
                if (rowSize <= 0) {
                    pt.x = rightEdge;
                    smallestRowSize = 0;
                    break;
                }

                if (rowSize < smallestRowSize)
                    smallestRowSize = rowSize;
            }

            // If the row size is not zero, we found free space.
            if (smallestRowSize > 0) break;
        }
        return smallestRowSize;
    }

    private void findSprites() {

        // Scan for the start of a frame, skipping areas where we
        // already found frames.

        Point scan = new Point(0,0);

        frameLoop:
            while (true) {
            // Is this pixel within an existing frame?
            int rowSize = skipExistingFrames(scan);
            if (rowSize == 0) break;

            int pixOffset = scan.x + scan.y * sprite.w;
            int pixOffsetStop = pixOffset + rowSize;

            do {
                int p = pix[pixOffset++];
                if (p != bgColor) {
                    // We found a non-transparent pixel.  Assume it
                    // is a frame border.
                    processFrame(scan);
                    if (VidGame.DEBUG) {
                        if (frameCount == MAX_SPRITES) {
                            Debug.print("too many frames found, stopping!");
                            break frameLoop;
                        }
                    }
                    break;
                }
                scan.x++;
            } while (pixOffset != pixOffsetStop);
        }

        constructSpriteRecords();

/*
        for (int y = 0; y < sprite.h; y++) {
            for (int x = 0; x < sprite.w; x++) {
                int p = pix[x+y*sprite.w];

            	int alpha = (p >> 24) & 0xff;
            	int red   = (p >> 16) & 0xff;
            	int green = (p >>  8) & 0xff;
            	int blue  = (p      ) & 0xff;

                Debug.print("X:"+x+" Y:"+y+" Alpha:"+alpha+" R:"+red+" G:"+green+" B:"+blue);
            }
        }
*/
    }

    // Determine the size of a frame
    // pt = location of top left corner
    private void processFrame(Point pt) {

        boolean cxFound = false;
        boolean cyFound = false;
        int cx = 0;
        int cy = 0;

        // Scan horizontally until we find a transparent pixel (or
        // the edge of the sprite)

        int pixOffset = pt.x + pt.y * sprite.w;
        int frameColor = pix[pixOffset];

        int frameWidth = sprite.w - pt.x;
        int i;
        for (i = 1; i < frameWidth; i++) {
            int color = pix[pixOffset + i];
            if (color == bgColor) {
                frameWidth = i;
                break;
            }
            if (!cxFound && color != frameColor) {
                cxFound = true;
                cx = i - 1;
            }

        }

        int frameHeight = sprite.h - pt.y;
        for (i = 1; i < frameHeight; i++) {
            pixOffset += sprite.w;
            int color = pix[pixOffset];
            if (color == bgColor) {
                frameHeight = i;
                break;
            }
            if (!cyFound && color != frameColor) {
                cyFound = true;
                cy = i - 1;
            }
        }

        int j = (frameCount++ * WORKFRAMELEN);
        workFrames[j+0] = pt.x;
        workFrames[j+1] = pt.y;
        workFrames[j+2] = frameWidth;
        workFrames[j+3] = frameHeight;
        workFrames[j+4] = pt.x + frameWidth;
        workFrames[j+5] = pt.y + frameHeight;

        if (!cxFound)
            cx = (frameWidth-2) >> 1;
        if (!cyFound)
            cy = (frameHeight-2) >> 1;

        workFrames[j+6] = cx;
        workFrames[j+7] = cy;

//        Debug.print("frame "+pt.x+","+pt.y+", "+frameWidth+","+frameHeight+" cx = "+cx+" cy="+cy);

        pt.x += frameWidth;
    }

    private void grabPixels() {
        pix = new int[sprite.w * sprite.h];

        PixelGrabber pg = new PixelGrabber(
            sprite.image,
            sprite.ox,sprite.oy,
            sprite.w,sprite.h,
            pix,0,sprite.w);

    	try {
	        pg.grabPixels();
    	} catch (InterruptedException e) {}
/*    	if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
	    System.err.println("image fetch aborted or errored");
	    return; */
    }

    private void constructSpriteRecords() {
        spriteRecs = new short[frameCount * SPRITERECLEN];
        for (int i = 0; i < frameCount; i++)
            calcShrunkenSprite(i);
    }

    // Calculate the position, size, and centerpoint of the sprite,
    // eliminating any empty rows and columns.
    //
    // Store the shrunken information in the spriteRecs array.
    //
    private void calcShrunkenSprite(int i) {
        int j = i * WORKFRAMELEN;
        int k = i * SPRITERECLEN;

        int x = workFrames[j+0] + 1;
        int w = workFrames[j+2] - 2;
        int y = workFrames[j+1] + 1;
        int h = workFrames[j+3] - 2;

        int cx = workFrames[j+6];
        int cy = workFrames[j+7];

        // If the bottom right pixel of the frame is missing,
        // don't perform this shrinking process.  Some sprites, like
        // character sets, expect certain frame sizes for their
        // calculations.

        if (pix[(y + h) * sprite.w + (x + w)] != bgColor) {

            // Scan pixels to eliminate empty rows and columns.

            while (h > 0 && rowIsEmpty(x,y,w)) {
                y++;
                h--;
                cy--;
            }
            while (h > 0 && rowIsEmpty(x,y+h-1,w))
                h--;
            while (w > 0 && columnIsEmpty(x,y,h)) {
                x++;
                w--;
                cx--;
            }
            while (w > 0 && columnIsEmpty(x+w-1,y,h))
                w--;
        }

        spriteRecs[k+0] = (short)x;
        spriteRecs[k+1] = (short)y;
        spriteRecs[k+2] = (short)w;
        spriteRecs[k+3] = (short)h;
        spriteRecs[k+4] = (short)cx;
        spriteRecs[k+5] = (short)cy;
    }

    private boolean rowIsEmpty(int x, int y, int width) {
        int pixOffset = y * sprite.w + x;

        while (width > 0) {
            if (pix[pixOffset++] != bgColor) break;
            width--;
        }

        return (width == 0);
    }

    private boolean columnIsEmpty(int x, int y, int height) {
        int width = sprite.w;
        int pixOffset = y * width + x;
        while (height > 0) {
            if (pix[pixOffset] != bgColor) break;
            pixOffset += width;
            height--;
        }
        return (height == 0);
    }

    private Sprite sprite;
    private short[] spriteRecs;
    private int frameCount;
    private int[] pix;
    private int bgColor;
    private int[] workFrames;
    private static final int MAX_SPRITES = 200;
    private static final int WORKFRAMELEN = 8;
    private static final int SPRITERECLEN = 6;
}