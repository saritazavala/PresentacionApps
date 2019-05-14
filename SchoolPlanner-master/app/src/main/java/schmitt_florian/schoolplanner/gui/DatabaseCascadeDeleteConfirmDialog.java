package schmitt_florian.schoolplanner.gui;


import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import schmitt_florian.schoolplanner.R;

public class DatabaseCascadeDeleteConfirmDialog extends AlertDialog {



    public DatabaseCascadeDeleteConfirmDialog(@NonNull Context context, int numberOfObjects) {
        super(context);
        setTitle(getContext().getResources().getString(R.string.string_confirm_delete));
        setMessage(getContext().getResources().getString(R.string.text_dialog_db_cascade_deletion_part1) +
                " " + numberOfObjects + " " + getContext().getResources().getString(R.string.text_dialog_db_cascade_deletion_part2));
        setIcon(android.R.drawable.ic_menu_delete);
        setCancelable(false);
        setButton(BUTTON_NEGATIVE, getContext().getResources().getString(R.string.string_cancel), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
    }


    public void positiveButton(OnClickListener onClickListener) {
        setButton(BUTTON_POSITIVE, getContext().getResources().getString(R.string.string_delete), onClickListener);
    }

}
