package vgpackage;
import java.awt.*;
import java.util.*;
import mytools.*;

public class CharSet {

	// Constructor
	// Precondition:
	//	sprite = sprite containing character bitmaps
	//	width,height = size of each character, in pixels
	//	spritePadX,spritePadY = # pixels padding between each character, as they appear in the
	//		sprite Image
	//	charList = characters associated with bitmaps
	public CharSet(
		Sprite sprite, int width, int height, int spritePadX, int spritePadY,
		String charList
	) {
		this.sizeX = width;
		this.sizeY = height;
		this.spritePadX = spritePadX;
		this.spritePadY = spritePadY;
		this.spacingX = spritePadX;
		this.spacingY = spritePadY;
		this.sprite = sprite;
		this.charList = charList;
		this.charListLen = charList.length();
		this.charsWide = sprite.w / (sizeX + spritePadX);
		if (VidGame.DEBUG)
			Debug.ASSERT(charListLen <= charsWide * (sprite.h / (sizeY + spritePadY)), "CharSet:not all chars fit in sprite");

		// Construct a sprite that we will use to act as a window into the character set.

		plotSprite = new Sprite(sprite, 0, 0, sizeX, sizeY, 0, 0);
		ox = plotSprite.ox;
		oy = plotSprite.oy;
	}

	public void setSpacingX(int n) {
		spacingX = n;
	}

    private static Pt iPos = new Pt();
	public void plotChar(char c, int sx, int sy) {
		if (!findCharSprite(c, iPos)) return;

		// Manipulate the plotSprite image offsets to point to this character.

		plotSprite.ox = ox + iPos.x;
		plotSprite.oy = oy + iPos.y;

		BEngine.drawSprite(plotSprite, sx, sy - sizeY);
	}

	public void centerString(String s, int sx, int sy) {
		int strLen = s.length();

		int pixelsWide = getAdvX() * strLen - spacingX;
		int pixelsTall = sizeY;

		int startX = sx - (pixelsWide >> 1);
		int startY = sy;

		plotString(s, startX, startY);
	}

	// Clear the rectangle that a plotted string would occupy to current color
	public void clearBounds(int sx, int sy, int strLen) {
		Rectangle r = new Rectangle(sx, sy-sizeY, strLen * getAdvX() - spacingX, sizeY);
		BEngine.fillRect(r.x, r.y, r.width, r.height);
	}

    // Determine bounding rectangle of string
    public Point stringBounds(String s) {
        return new Point(
            s.length() * getAdvX() - spacingX, sizeY);
    }

	public void plotString(String s, int sx, int sy) {

        Point size = stringBounds(s);
		int advX = getAdvX();

		Rectangle r = new Rectangle(sx,sy - sizeY,
            size.x,size.y);

		BEngine.disableUpdate(1);

        int k = s.length();
		for (int i = 0; i < k; i++) {
			char c = s.charAt(i);

			plotChar(c, sx, sy);
			sx += advX;
		}

		BEngine.disableUpdate(-1);

		BEngine.updateRect(r.x, r.y, r.width, r.height);
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getAdvX() {
		return (sizeX + spacingX);
	}

	public int getSizeY() {
		return sizeY;
	}

	public int getSpacingX() {
		return spacingX;
	}

	// Find location of character in bitmap
	// Precondition:
	//	c = char to find
	// Postcondition:
	//	returns false if not found, else loc is the position of the top left pixel
	private boolean findCharSprite(char c, Pt loc) {

		for (int i = charListLen-1; i >= 0; i--) {
			if (c != charList.charAt(i)) continue;

			// Determine x,y location of this character within the sprite.

			loc.x = (i % charsWide) * (sizeX + spritePadX);
			loc.y = (i / charsWide) * (sizeY + spritePadY);
			return true;
		}
		return false;
	}

	// Private members:

	private int sizeX, sizeY;
	private int spacingX, spacingY;
	private int spritePadX, spritePadY;
	private Sprite sprite;
	private String charList;
	private int charListLen;
	private int charsWide;
	private Sprite plotSprite;
	private int ox,oy;			// initial values of plotSprite.ox,oy
}
