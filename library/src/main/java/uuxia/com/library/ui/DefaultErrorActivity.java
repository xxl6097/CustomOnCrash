/*
 * Copyright 2015 Eduard Ereza Martínez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uuxia.com.library.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import uuxia.com.library.CustomOnCrashCore;
import uuxia.com.library.R;
import uuxia.com.library.mail.Email;


/**
 * The type Default error activity.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public final class DefaultErrorActivity extends Activity implements PopupMenu.OnMenuItemClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.customactivityoncrash_default_error_activity);

        //Close/restart button logic:
        //If a class if set, use restart.
        //Else, use close and just finish the app.
        //It is recommended that you follow this logic if implementing a custom error activity.
        initUuxiaView();
    }

    /**
     * Showpop.
     *
     * @param v the v
     */
    public void showpop(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.copy) {
            copyErrorToClipboard();
            return true;
        } else if (id == R.id.email){
            popEmail();
            return true;
        }else if (id == R.id.restart){
            final Class<? extends Activity> restartActivityClass = CustomOnCrashCore.getRestartActivityClassFromIntent(getIntent());
            if (restartActivityClass != null) {
                Intent intent = new Intent(DefaultErrorActivity.this, restartActivityClass);
                CustomOnCrashCore.restartApplicationWithIntent(DefaultErrorActivity.this, intent);
            }
            return true;
        }
        return false;
    }


    private void initUuxiaView(){
        TextView errorDetailsText = (TextView) findViewById(R.id.error_details);
        String allerr = CustomOnCrashCore.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());
        String traceerr = CustomOnCrashCore.getStackTraceFromIntent(getIntent());
        errorDetailsText.setText(allerr);

        Button restartButton = (Button) findViewById(R.id.restart_button);

        final Class<? extends Activity> restartActivityClass = CustomOnCrashCore.getRestartActivityClassFromIntent(getIntent());

        if (restartActivityClass != null) {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomOnCrashCore.closeApplication(DefaultErrorActivity.this);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomOnCrashCore.closeApplication(DefaultErrorActivity.this);
                }
            });
        }

        TextView more = (TextView) findViewById(R.id.more);
        more.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
        more.getPaint().setAntiAlias(true);//抗锯齿
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showpop(v);
            }
        });
    }


    private void copyErrorToClipboard() {
        String errorInformation =
                CustomOnCrashCore.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
        } else {
            //noinspection deprecation
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(errorInformation);
        }
    }

    private void popEmail(){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.fillmail, null);

        final EditText from = (EditText) layout.findViewById(R.id.from);
        final EditText pass = (EditText) layout.findViewById(R.id.pass);
        final EditText to = (EditText) layout.findViewById(R.id.to);

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("please input information");
        dlg.setView(layout);
        dlg.setPositiveButton("send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fromAddr = from.getText().toString();
                String password = pass.getText().toString();
                String toAddr = to.getText().toString();
                if (TextUtils.isEmpty(fromAddr) || TextUtils.isEmpty(password) || TextUtils.isEmpty(toAddr)) {
                    Toast.makeText(DefaultErrorActivity.this, "please input information", Toast.LENGTH_SHORT).show();
                    return;
                }
                Email mail = Email.create(toAddr, fromAddr, password);

                String errorInformation = CustomOnCrashCore.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());
                CustomOnCrashCore.sendMail(DefaultErrorActivity.this,errorInformation,true,mail);
            }
        });
        dlg.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dlg.show();
    }
}
