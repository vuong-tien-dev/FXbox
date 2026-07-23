package com.vtstudio.fxbox.fxviews.tools;

import android.app.DialogFragment;

/**
 * Created by Álvaro Blanco Cabrero on 02/05/2017.
 * Zoomy.
 */

public class DialogFragmentContainer extends DialogContainer {

    DialogFragmentContainer(DialogFragment dialog) {
        super(dialog.getDialog());
    }

}
