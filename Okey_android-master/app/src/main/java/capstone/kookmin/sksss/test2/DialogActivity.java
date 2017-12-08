package capstone.kookmin.sksss.test2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

public class DialogActivity extends Activity {

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);

        Intent intent = getIntent();
        String[] dictionaryContents = intent.getExtras().getStringArray("dictContents");

        final String dictionaryUrl = dictionaryContents[0];
        String dictionaryText = dictionaryContents[1];

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("네이버 백과사전");

        alertDialogBuilder
                .setMessage(dictionaryText)
                .setCancelable(true)
                .setPositiveButton("더보기",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                stopService(getIntent());
                                Intent webDictionaryIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dictionaryUrl));
                                startActivity(webDictionaryIntent);
                                finish();
                            }
                        })
                .setNegativeButton("닫기",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                stopService(getIntent());
                                dialog.cancel();
                                finish();
                            }
                })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                stopService(getIntent());
                                dialog.cancel();
                                finish();
                            }
                        });

        alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }
}
