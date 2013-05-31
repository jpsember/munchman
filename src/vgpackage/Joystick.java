package vgpackage;
import java.awt.event.*;
public class Joystick {
	private int flags;

	private static final int BUTTON0 = 4;
	private static final int CLICK0 = 6;

    private static final int MOUSEBUTTON0 = 8;
    private static final int MOUSECLICK0 = 10;

	public Joystick() {
		flags = 0;
	}

	// Adjust the states of our simulated 4-button joystick, plus the one fire button.
	// Preconditions:
	//	code: KeyEvent code detected
	//	pressFlag: true if key has been pressed, false if released
	// Postcondition:
	//	flags has been modified accordingly
	public void processKeyEvent(KeyEvent e, boolean pressFlag) {
		final int KEYCODE_TOTAL = 6;
		final int keyCodes[] = {
			KeyEvent.VK_UP,
			KeyEvent.VK_RIGHT,
			KeyEvent.VK_DOWN,
			KeyEvent.VK_LEFT,
			KeyEvent.VK_F,
			KeyEvent.VK_SPACE,
		};

		int prevFlags = flags;

		for (int i=0; i<KEYCODE_TOTAL; i++) {
			if (e.getKeyCode() == keyCodes[i]) {
				if (pressFlag) {
					if (i < 4)
						flags &= ~(1 << (i ^ 2));

					flags |= (1 << i);
				} else
					flags &= ~(1 << i);
				break;
			}
		}

		// If fire button is now pressed, and it wasn't before, set 'buttonClick' flag.
		for (int i = 0; i < 2; i++) {
			if ((prevFlags & (1 << (BUTTON0 + i))) == 0 && (flags & (1 << (BUTTON0 + i))) != 0)
			flags |= (1 << (CLICK0 + i));
		}
	}

	// Determine if a fire button is currently pressed
	public boolean fireButtonState(int n) {
		return (flags & ((1 << MOUSEBUTTON0) | (1 << (n+BUTTON0)))) != 0;
	}

	// Determine the x-position of the current joystick reading
	// Postcondition:
	//	returns -1 if to the left, 0 if centered, 1 if to the right
	public int xPos() {
		final int values[] = {0,0,1,1,1,0,-1,-1,-1};

		return values[pos()];
	}

	// Determine the y-position of the current joystick reading
	// Postcondition:
	//	returns -1 if up, 0 if centered, 1 if down
	public int yPos() {
		final int values[] = {0,-1,-1,0,1,1,1,0,-1};
		return values[pos()];
	}

	// Determine which of the 9 positions the joystick is currently in.
	// Postcondition:
	//	returns 0..8, corresponding to:
	//		8 1 2
	//		7 0 3
	//		6 5 4
	//	If an 'impossible' joystick position was detected (i.e. both
	//	the left and right buttons are pressed), both buttons are cancelled out
	//	in that dimension.
	//
	public int pos() {
		// This table converts all possible bit flags 0000...1111 for each
		// of the four buttons to a joystick position 0...8, and 'cancels out' the
		// impossible positions as required.
		final int values[] = {0,1,3,2,5,0,4,3,7,8,0,1,6,7,5,0};
		return values[flags & 0x0f];
	}

	public boolean fireButtonClicked(int n) {
		int flag = (1 << (n+CLICK0)) | (1 << MOUSECLICK0);
		boolean result = (flags & flag) != 0;
		flags &= ~flag;
		return result;
	}

	public void clear() {
		// Clear the click flags.
		flags &= ~((1|2) << CLICK0);
	}

    public void processMouseButtonEvent(boolean pressed) {
		int prevFlags = flags;
        if (pressed)
            flags |= (1 << MOUSEBUTTON0);
        else
            flags &= ~(1 << MOUSEBUTTON0);
		// If fire button is now pressed, and it wasn't before, set 'buttonClick' flag.
		if ((prevFlags & (1 << MOUSEBUTTON0)) == 0 &&
            (flags & (1 << MOUSEBUTTON0)) != 0)
			flags |= (1 << MOUSECLICK0);
    }

}
