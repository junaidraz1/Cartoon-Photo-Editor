package com.miczon.cartoonme.Listeners;

import android.app.AlertDialog;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

/**
 * To handle dialog related click events
 */
public interface DialogClickListener {

    void onButtonClick(String status, String message, String data, AlertDialog alertDialog);

}
