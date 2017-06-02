package sg.edu.rp.c347.smsretriever;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class FragmentSecond extends Fragment {
    //referring to the content of the sms
    TextView tvSms;
    Button btnRetrieve, btnEmail;
    EditText etWord;


    public FragmentSecond() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_second, container, false);

        tvSms = (TextView) view.findViewById(R.id.tvFrag2);
        btnRetrieve = (Button) view.findViewById(R.id.btnRetrieveFrag2);
        etWord = (EditText) view.findViewById(R.id.etFrag2);
        btnEmail = (Button) view.findViewById(R.id.btnEmail2);

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                String[] recipients = new String[]{"jason_lim@rp.edu.sg"};
                i.setData(Uri.parse("mailto:"));
                i.setType("message/rfc822");

                i.putExtra(Intent.EXTRA_EMAIL, recipients);
                i.putExtra(Intent.EXTRA_SUBJECT, "SMS CONTENT");
                i.putExtra(Intent.EXTRA_TEXT, tvSms.getText().toString());

                try {
                    startActivity(Intent.createChooser(i, "Sending email"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "No email client installed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = PermissionChecker.checkSelfPermission
                        (getActivity(), Manifest.permission.READ_SMS);

                if (permissionCheck != PermissionChecker.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_SMS}, 0);

                    return;
                }

                String text = etWord.getText().toString();
                Uri uri = Uri.parse("content://sms");

                String[] reqCols = new String[]{"date", "address", "body", "type"};

                // Get Content Resolver object from which to query the content provider
                ContentResolver cr = getActivity().getContentResolver();

                String filter = "body LIKE ? ";
                String[] separated = text.split(" ");
                for (int i = 0; i < separated.length; i++) {
                    separated[i] = "%" + separated[i] + "%";
                    if (i != 0) {
                        filter += " OR body LIKE ? ";
                    }
                }

                String[] filterArgs = separated;


                Cursor cursor = cr.query(uri, reqCols, filter, filterArgs, null);

                String smsBody = "";

                if (cursor.moveToFirst()) {
                    do {
                        long dateInMillis = cursor.getLong(0);
                        String date = (String) DateFormat
                                .format("dd MMM yyyy h:mm:ss aa", dateInMillis);
                        String address = cursor.getString(1);
                        String body = cursor.getString(2);
                        String type = cursor.getString(3);
                        if (type.equalsIgnoreCase("1")) {
                            type = "Inbox:";
                        } else {
                            type = "Sent:";
                        }
                        smsBody += type + " " + address + "\n at " + date
                                + "\n\"" + body + "\"\n\n";
                    } while (cursor.moveToNext());
                }
                tvSms.setText(smsBody);
            }


        });


        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnRetrieve.performClick();

                } else {
                    Toast.makeText(getActivity(), "Permission not granted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
