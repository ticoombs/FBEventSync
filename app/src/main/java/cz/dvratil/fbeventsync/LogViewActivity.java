package cz.dvratil.fbeventsync;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import cz.dvratil.fbeventsync.BuildConfig;

public class LogViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logview, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadLogFile();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_log:
                Logger.getInstance(this).clearLogs();
                loadLogFile();
                return true;

            case R.id.action_send_to_develop:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "me@dvratil.cz" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "FBEventSync logs");

                intent.putExtra(
                        Intent.EXTRA_TEXT,
                        String.format(
                                Locale.US,
                                "App ID: %s\n" +
                                        "App version: %d (%s)\n" +
                                        "App build: %s\n" +
                                        "OS: %s %s (API %d)\n",
                                BuildConfig.APPLICATION_ID,
                                BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME,
                                BuildConfig.BUILD_TYPE,
                                Build.VERSION.BASE_OS, Build.VERSION.RELEASE, Build.VERSION.SDK_INT));

                File logFile = new File(getFilesDir(), Logger.LOG_FILE);
                if (!logFile.exists() || !logFile.canRead()) {
                    Toast.makeText(this, R.string.toast_error_sending_log, Toast.LENGTH_SHORT).show();
                    return false;
                }

                Uri contentUri = FileProvider.getUriForFile(
                        this, "cz.dvratil.fbeventsync.FileProvider", logFile);
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.intent_send_email)));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadLogFile() {
        TextView textView = (TextView) findViewById(R.id.logtextview);
        File file = new File(getFilesDir(), Logger.LOG_FILE);
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(file));
            try {
                String line;
                while ((line = buffer.readLine()) != null) {
                    builder.append(line);
                    builder.append('\n');
                }
            } catch (Exception e) {
                Log.e("LOGVIEW", "Exception when reading log: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e("LOGVIEW", "Exception when opening log: " + e.getMessage());
        }

        textView.setText(builder.toString());
    }
}