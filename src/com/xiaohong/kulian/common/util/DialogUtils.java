package com.xiaohong.kulian.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.xiaohong.kulian.R;
import com.xiaohong.kulian.common.widget.CustomDialog;
import com.xiaohong.kulian.ui.DialogActivity;

/**
 * <p>
 * Dialog util
 * </p>
 * 
 * @author llh
 */
public class DialogUtils {
    
    public static final String KEY_DIALOG_TITLE = "title";
    public static final String KEY_DIALOG_MESSAGE = "message";
    
    public static void showMessage(Context context, String title, String message) {

        Intent intent = new Intent(context, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DialogUtils.KEY_DIALOG_TITLE, title);
        intent.putExtra(DialogUtils.KEY_DIALOG_MESSAGE, message);
        context.startActivity(intent);
    }

    public static interface WarningDialogListener {
        
        public void onWarningDialogOK(int id);

        public void onWarningDialogCancel(int id);
    }

    public static interface ProgressDialogListener {
        
        public void onProgressDialogCancel(int id);
    }

    public static Dialog createIndeterminateProgressWhiteTextDialog(final Context context,
            final int id, String hint, boolean cancelable) {
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.alert_dialog_indeterminate_progress_white_text, null);

        // set hint
        if (hint == null) {
            throw new RuntimeException("Must provide a hint string for input dialog");
        }
        TextView tvHint = (TextView) view.findViewById(R.id.tv_hint);
        tvHint.setText(hint);

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(cancelable)
                .setView(view);

        // if (cancelable) {
        // builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog, int whichButton) {
        // if (context instanceof Activity)
        // ((Activity) context).removeDialog(id);
        //
        // if (listener != null)
        // listener.onProgressDialogCancel(id);
        // }
        // });
        // builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        // public void onCancel(DialogInterface arg0) {
        // if (context instanceof Activity)
        // ((Activity) context).removeDialog(id);
        //
        // if (listener != null)
        // listener.onProgressDialogCancel(id);
        // }
        // });
        // }

        Dialog dialog = builder.create();

        // if (!cancelable) {
        // dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
        // public void onDismiss(DialogInterface arg0) {
        // if (context instanceof Activity)
        // ((Activity) context).removeDialog(id);
        // }
        // });
        // }

        return dialog;
    }

    public static interface YesNoDialogListener {
        public void onYesDialog(int id);

        public void onNoDialog(int id);
    }

    public static interface CheckBoxWarningDialogListener {
        public void onWarningDialogOK(int id, boolean checked);

        public void onWarningDialogCancel(int id);
    }

    public static interface InfoDialogListener {
        public void onInfoDialogOK(int id);
    }

    public static interface InputDialogListener {
        public void onInputDialogOK(int id, String value);

        public void onInputDialogCancel(int id);
    }

    public static interface EditTextDialogListener {
        public void onEditTextDialogOK(int id, String value);

        public void onEditTextDialogCancel(int id);
    }

    public static interface UserPwdDialogListener {
        public void onUserPwdDialogOK(int id, String user, String pwd, boolean isChecked);

        public void onUserPwdDialogCancel(int id);

        public void onUserPwdDialogRegister(int id);
    }

    public static interface RegisterDialogListener {
        public void onRegisterDialogOK(int id, String user, String pwd1, String pwd2);

        public void onRegisterDialogCancel(int id);
    }

    public static interface ListCheckboxDialogListener {
        public void onListDialogOK(int id, CharSequence[] items, int selectedId, int selectedIndex);

        public void onListDialogCancel(int id, CharSequence[] items);
    }

    public static interface ListDIalogListener {
        public void onListDialogOK(int id, int which);
    }

    public static interface ListDialogListener2 {
        public void onListDialogOK2(int id, Object[] items, int selectedItem);

        public void onListDialogCancel2(int id, Object[] items);
    }

    public static interface RatingDialogListener {
        public void onRatingDialogOK(int id, float ratings);

        public void onRatingDialogCancel();
    }

    private static int mWhich;
    private static int[] mItemIds;
    private static int mRating;

    public static ProgressDialog createDeterminateProgressDialog(final Context context, final int id, String hint, boolean cancelable,
            final ProgressDialogListener listener) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setIcon(android.R.drawable.ic_dialog_info);
        pd.setTitle(hint);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        if (cancelable) {
            pd.setButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onProgressDialogCancel(id);
                }
            });
            pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onProgressDialogCancel(id);
                }
            });
        }

        if (!cancelable) {
            pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);
                }
            });
        }

        return pd;
    }

    /**
     * <p>
     * Create a warning dialog which only has one OK button
     * </p>
     * 
     * @param context
     *            {@link Context} object
     * @param id
     *            dialog id
     * @param warning
     *            warning message
     * @return a warning dialog instance
     */
    public static Dialog createOKWarningDialog(final Context context, final int id, String warning,
            final WarningDialogListener listener) {
        /*
         * Common warning dialog Only one OK buttons, one message, an icon
         */
        return new AlertDialog.Builder(context).setCancelable(false)
                .setTitle(R.string.attention).setMessage(warning)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (context instanceof Activity)
                            ((Activity) context).removeDialog(id);

                        if (listener != null)
                            listener.onWarningDialogOK(id);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        if (context instanceof Activity)
                            ((Activity) context).removeDialog(id);

                        if (listener != null)
                            listener.onWarningDialogOK(id);
                    }
                }).create();
    }

    public static Dialog createIndeterminateProgressDialog(final Context context, final int id, String hint, boolean cancelable,
            final ProgressDialogListener listener) {
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.alert_dialog_indeterminate_progress_white_text, null);

        // set hint
        if (hint == null) {
            throw new RuntimeException("Must provide a hint string for input dialog");
        }
        TextView tvHint = (TextView) view.findViewById(R.id.tv_hint);
        tvHint.setText(hint);

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(cancelable).setView(view);

        if (cancelable) {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onProgressDialogCancel(id);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onProgressDialogCancel(id);
                }
            });
        }

        Dialog dialog = builder.create();

        if (!cancelable) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);
                }
            });
        }

        return dialog;
    }

    /**
     * <p>
     * Create a warning dialog
     * </p>
     * 
     * @param context
     *            {@link Context} object
     * @param id
     *            dialog id
     * @param warning
     *            warning message, should not be null
     * @param listener
     *            {@link WarningDialogListener} instance, should not be null
     * @return a warning dialog instance
     */
    public static Dialog createYesNoWarningDialog(final Context context, final int id, String warning, final WarningDialogListener listener) {
        /*
         * Common warning dialog Two buttons, one message, an icon
         */
        return new AlertDialog.Builder(context).setTitle(R.string.attention).setMessage(warning).setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (context instanceof Activity) {
                            ((Activity) context).removeDialog(id);
                        }

                        if (listener != null)
                            listener.onWarningDialogOK(id);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onWarningDialogCancel(id);
                }
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }
            }
        }).create();
    }

    /**
     * <p>
     * Create a info dialog which only has one OK button
     * </p>
     * 
     * @param context
     *            {@link Context} object
     * @param id
     *            dialog id
     * @param info
     *            info message
     * @return a info dialog instance
     */
    public static Dialog createInfoDialog(final Context context, final int id, String info,
            final InfoDialogListener listener) {
        /*
         * Common warning dialog Only one OK buttons, one message, an icon
         */
        return new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(context.getString(R.string.info)).setMessage(info)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (context instanceof Activity)
                            ((Activity) context).removeDialog(id);

                        if (listener != null)
                            listener.onInfoDialogOK(id);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        if (context instanceof Activity)
                            ((Activity) context).removeDialog(id);

                        if (listener != null)
                            listener.onInfoDialogOK(id);
                    }
                }).create();
    }

    /**
     * <p>
     * Create a YesNo dialog
     * </p>
     * 
     * @param context
     *            {@link Context} object
     * @param id
     *            dialog id
     * @param info
     *            info message
     * 
     * @return a YesNo dialog instance
     */
    public static Dialog createYesNoDialog(final Context context, final int id, String info, final YesNoDialogListener listener) {
        return new AlertDialog.Builder(context).setIcon(R.drawable.alert_dialog_icon).setTitle(info).setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (context instanceof Activity)
                            ((Activity) context).removeDialog(id);

                        if (listener != null)
                            listener.onYesDialog(id);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity)
                    ((Activity) context).removeDialog(id);

                if (listener != null)
                    listener.onNoDialog(id);
            }
        }).create();
    }

    public static Dialog createShowHintOKDialog(final Context context, final int id, String title, String warning) {
        /*
         * Common warning dialog Only one OK buttons, one message, an icon
         */
        return new AlertDialog.Builder(context).setTitle(title).setMessage(warning).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity)
                    ((Activity) context).removeDialog(id);
            }
        }).create();
    }

    public static Dialog createListDialog(final Context context, final int id, int stringArrayId, final ListDIalogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setItems(stringArrayId, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (context instanceof Activity)
                    ((Activity) context).removeDialog(id);

                if (listener != null)
                    listener.onListDialogOK(id, which);
            }
        });

        return builder.create();
    }

    /**
     * <p>
     * Create an input dialog
     * </p>
     * 
     * @param context
     *            {@link Context} object
     * @param id
     *            dialog id
     * @param hint
     *            hint message
     * @param initValue
     *            initial string value in text box, can be null if no initial value
     * @param secure
     *            true if you want to input password
     * @param listener
     *            {@link InputDialogListener} instance, should not be null
     * @return an input dialog instance
     */
    public static Dialog createInputDialog(final Context context, final int id, String hint, String initValue, boolean secure,
            final InputDialogListener listener) {
        /*
         * Common input dialog Two buttons, one edittext, one hint, and title
         */

        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.alert_dialog_text_entry, null);

        // set hint
        if (hint == null) {
            throw new RuntimeException("Must provide a hint string for input dialog");
        }
        TextView tvHint = (TextView) view.findViewById(R.id.tv_hint);
        tvHint.setText(hint);

        // set init value
        final EditText etInput = (EditText) view.findViewById(R.id.et_input);
        if (initValue != null) {
            etInput.setText(initValue);
        }
        if (secure)
            etInput.setTransformationMethod(PasswordTransformationMethod.getInstance());

        return new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.app_download).setView(view).setPositiveButton(
                R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = etInput.getText().toString();
                        if (context instanceof Activity) {
                            ((Activity) context).removeDialog(id);
                        }

                        if (listener != null)
                            listener.onInputDialogOK(id, value);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onInputDialogCancel(id);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onInputDialogCancel(id);
            }
        }).create();
    }

    public static Dialog createInputDialog(final Context context, final int id, String hint, String initValue, String title, String hintValue, boolean secure,
            final InputDialogListener listener) {
        /*
         * Common input dialog Two buttons, one edittext, one hint, and title
         */

        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.alert_dialog_text_entry, null);

        // set hint
        if (hint == null) {
            throw new RuntimeException("Must provide a hint string for input dialog");
        }
        TextView tvHint = (TextView) view.findViewById(R.id.tv_hint);
        tvHint.setText(hint);
        tvHint.setVisibility(View.GONE);

        // set init value
        final EditText etInput = (EditText) view.findViewById(R.id.et_input);
        if (initValue != null) {
            etInput.setText(initValue);
        }
        if (hintValue != null) {
            etInput.setHint(hintValue);
        }
        if (secure)
            etInput.setTransformationMethod(PasswordTransformationMethod.getInstance());

        return new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title).setView(view).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = etInput.getText().toString();
                        if (context instanceof Activity) {
                            ((Activity) context).removeDialog(id);
                        }

                        if (listener != null)
                            listener.onInputDialogOK(id, value);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onInputDialogCancel(id);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onInputDialogCancel(id);
            }
        }).create();
    }

    public static Dialog createRatingDialog(final Context context, final int id, int initRating, int ratingCount, final RatingDialogListener listener) {
        mRating = initRating;
        LayoutInflater vi = LayoutInflater.from(context);
        View view = vi.inflate(R.layout.alert_dialog_ratings_entry, null, false);
        TextView tvRating = (TextView) view.findViewById(R.id.tv_ratingCount);
        tvRating.setText(context.getString(R.string.hint_rating_count, ratingCount));
        final TextView tvRatingHint = (TextView) view.findViewById(R.id.rating_level_hint);
        tvRatingHint.setText(context.getResources().getStringArray(R.array.rating)[mRating - 1]);
        final RatingBar rb = (RatingBar) view.findViewById(R.id.rb_click);
        rb.setRating(initRating);
        rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    mRating = (int) rating;
                    ratingBar.setRating(mRating);
                }
                tvRatingHint.setText(context.getResources().getStringArray(R.array.rating)[mRating - 1 >= 0 ? mRating - 1 : 0]);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.rating);
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null) {
                    listener.onRatingDialogOK(id, mRating);
                }

            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onRatingDialogCancel();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onRatingDialogCancel();
            }
        });
        return builder.create();
    }

    public static Dialog createBigInputDialog(final Context context, final int id, final int titleID, final InputDialogListener listener) {
        /*
         * Common input dialog Two buttons, one edittext, one hint, and title
         */

        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.alert_dialog_big_input, null);

        // set init value
        final EditText etInput = (EditText) view.findViewById(R.id.et_input);

        return new AlertDialog.Builder(context).setTitle(titleID).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = etInput.getText().toString();
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onInputDialogOK(id, value);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);
                }

                if (listener != null)
                    listener.onInputDialogCancel(id);
            }
        }).create();
    }

    public static Dialog createIndeterminateProgressWhiteTextDialog(final Context context, final int id, String hint, boolean cancelable,
            final ProgressDialogListener listener) {
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.alert_dialog_indeterminate_progress_white_text, null);

        // set hint
        if (hint == null) {
            throw new RuntimeException("Must provide a hint string for input dialog");
        }
        TextView tvHint = (TextView) view.findViewById(R.id.tv_hint);
        tvHint.setText(hint);

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(cancelable).setView(view);

        if (cancelable) {
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onProgressDialogCancel(id);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onProgressDialogCancel(id);
                }
            });
        }

        Dialog dialog = builder.create();

        if (!cancelable) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface arg0) {
                    if (context instanceof Activity)
                        ((Activity) context).removeDialog(id);
                }
            });
        }

        return dialog;
    }

    public static Dialog createYesNo2TVDialog(final Context context, final int id, String hint, String warning, final WarningDialogListener listener) {
        View view = new LinearLayout(context);
        LayoutInflater vi = LayoutInflater.from(context);
        view = vi.inflate(R.layout.alert_dialog_2_tv, null, false);
        TextView tv_hint = (TextView) view.findViewById(R.id.tv_hint);
        TextView tv_warning = (TextView) view.findViewById(R.id.tv_warning);
        tv_hint.setTextColor(Color.WHITE);
        tv_hint.setText(hint);
        tv_warning.setTextColor(Color.RED);
        tv_warning.setText(warning);
        return new AlertDialog.Builder(context).setView(view).setTitle(R.string.attention).setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (context instanceof Activity) {
                            ((Activity) context).removeDialog(id);
                        }

                        if (listener != null)
                            listener.onWarningDialogOK(id);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (context instanceof Activity) {
                    ((Activity) context).removeDialog(id);

                    if (listener != null)
                        listener.onWarningDialogCancel(id);
                }
            }
        }).create();
    }
}
