package com.example.basvuru_sistemi_yazilim_gelistirme;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.example.basvuru_sistemi_yazilim_gelistirme.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private AppBarConfiguration mAppBarConfiguration;
    private TextView dateOfBirthText;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private Spinner spinner2;
    private ArrayAdapter<CharSequence> major;
    private DatabaseReference mDatabase;
    private Spinner spinner;
    private EditText userMail;
    private EditText userPassword;
    private EditText userName;
    private EditText userSurname;
    private Spinner userFaculty;
    private Spinner userMajor;
    private EditText userAddress;
    private EditText userPhoneNumber;
    private TextView userDateOfBirth;
    private Spinner userYear;
    private EditText userRegistryNumber;
    private EditText userStudentNumber;
    private User user;
    private int SELECT_PICTURE = 200;
    private int SELECT_FILE = 201;
    private ImageView IVPreviewImage;
    private FirebaseStorage storage;
    private UploadTask uploadTask;
    private Uri selectedImageUri;
    private Spinner application_type;
    private int clicked_button_id;
    private Uri cap_transkript_file, cap_puan_file, cap_signed_pdf;
    private String currentView;
    private String cap_faculty_text;
    private EditText emailField;
    private ListView listView;
    @Override
    protected void onStart() {
        super.onStart();
    }


    public void goToRegisterPage(View view) {
        setContentView(R.layout.signup_screen);
        findViewById(R.id.showDatePickerDialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        createSpinnerAdapters();
    }

    public void signInRedirect(View view) {
        // https://medium.com/@sinanyilmaz/android-firebase-authentication-email-parola-ve-google-cc154d1bf5fe
        setContentView(R.layout.login_screen);
        emailField = findViewById(R.id.emailSignIn);
        View sendPass = findViewById(R.id.textView14);
        sendPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mail = emailField.getText().toString().trim();

                if (TextUtils.isEmpty(mail)) {
                    Toast.makeText(getApplication(), "Lütfen email adresinizi giriniz.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(mail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Yeni parola için gerekli bağlantı adresinize gönderildi!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Mail gönderim hatası!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        EditText passwordField = findViewById(R.id.passwordSignIn);
        View signinButton = findViewById(R.id.signinButton);
        // This means user is either not registered or signed in.
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                final String parola = passwordField.getText().toString();

                //Email girilmemiş ise kullanıcıyı uyarıyoruz.
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Lütfen emailinizi giriniz", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Parola girilmemiş ise kullanıcıyı uyarıyoruz.
                if (TextUtils.isEmpty(parola)) {
                    Toast.makeText(getApplicationContext(), "Lütfen parolanızı giriniz", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Firebase üzerinde kullanıcı doğrulamasını başlatıyoruz
                //Eğer giriş başarılı olursa task.isSuccessful true dönecek ve MainActivity e geçilecek
                mAuth.signInWithEmailAndPassword(email, parola)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    setContentView(R.layout.application_selection_screen);
                                }
                                else {
                                    Log.e("Giriş Hatası",task.getException().getMessage());
                                }
                            }
                        });

            }
        });
    }

    public void createSpinnerAdapters () {
        Spinner spinner = (Spinner) findViewById(R.id.faculty_spinner);
        ArrayAdapter<CharSequence> faculty_adapter = ArrayAdapter.createFromResource(this,
                R.array.fakulteler, android.R.layout.simple_spinner_item);
        faculty_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(faculty_adapter);
        spinner2 = (Spinner) findViewById(R.id.major_spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setMajorAdapters(parent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner spinner3 = (Spinner) findViewById(R.id.year_spinner);
        ArrayAdapter<CharSequence> year_adapter = ArrayAdapter.createFromResource(this,
                R.array.year, android.R.layout.simple_spinner_item);
        faculty_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(year_adapter);
    }

    public void setMajorAdapters(AdapterView<?> parent) {
        Log.d("ADAPTER",parent.getSelectedItem().toString());
        if(parent.getSelectedItem().toString().equals("Fen - Edebiyat")) {
            major = ArrayAdapter.createFromResource(this,
                    R.array.fenedebiyat, android.R.layout.simple_spinner_item);
            major.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(major);
        }
        else if(parent.getSelectedItem().toString().equals("Eğitim")) {
            major = ArrayAdapter.createFromResource(this,
                    R.array.egitim, android.R.layout.simple_spinner_item);
            major.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(major);
        }
        else if(parent.getSelectedItem().toString().equals("Mühendislik")) {
            major = ArrayAdapter.createFromResource(this,
                    R.array.muhendislik, android.R.layout.simple_spinner_item);
            major.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(major);
        }
        else if(parent.getSelectedItem().toString().equals("Teknoloji")) {
            major = ArrayAdapter.createFromResource(this,
                    R.array.teknoloji, android.R.layout.simple_spinner_item);
            major.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(major);
        }
        else if(parent.getSelectedItem().toString().equals("İletişim")) {
            major = ArrayAdapter.createFromResource(this,
                    R.array.iletisim, android.R.layout.simple_spinner_item);
            major.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(major);
        }
    }

    public void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = dayOfMonth + "/" + month + "/" + year;
        dateOfBirthText = findViewById(R.id.textDateOfBirth);
        dateOfBirthText.setText(date);
    }

    public void imageChooser(View view) {
        ImageView userPhoto = findViewById(R.id.userPhotoView);
        userPhoto.setBackground(null);
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(i, "Fotoğraf Seçin"), SELECT_PICTURE);
        userPhoto.setImageURI(selectedImageUri);
    }
    public void fileChooser(View view) {

        Intent i = new Intent();
        i.setType("*/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        clicked_button_id = view.getId();
        startActivityForResult(Intent.createChooser(i, "Dosya Seçin"), SELECT_FILE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    ImageView userphoto = findViewById(R.id.userPhotoView);
                    userphoto.setImageURI(selectedImageUri);
                }
            }
            if (requestCode == SELECT_FILE) {
                if (currentView.equals("ÇAP")) {
                    switch (clicked_button_id) {
                        case R.id.cap_puan:
                            cap_puan_file = data.getData();
                        case R.id.cap_transkript:
                            cap_transkript_file = data.getData();
                    }
                }
                if(currentView.equals("Başvuru")) {
                    cap_signed_pdf = data.getData();
                }
            }
        }
    }

    public void downloadCapPdf(View view) {
        if (cap_puan_file != null && cap_transkript_file != null && cap_faculty_text != "") {
            printPdfCap(view);
        }
        else {
            Toast.makeText(MainActivity.this, "Lütfen dosyaları eksiksiz yükleyin ve geçerli bir fakülte girin!", Toast.LENGTH_LONG).show();
        }
    }

    public void signupButtonClick(View view) {
        userMail = (EditText) findViewById(R.id.editTextTextEmailAddress);
        userName = (EditText) findViewById(R.id.editTextName);
        userPassword = (EditText) findViewById(R.id.editTextPassword);
        userSurname = (EditText) findViewById(R.id.editTextSurname);
        userFaculty = (Spinner) findViewById(R.id.faculty_spinner);
        userMajor = (Spinner) findViewById(R.id.major_spinner);
        userAddress = (EditText) findViewById(R.id.editTextAddress);
        userPhoneNumber = (EditText) findViewById(R.id.editTextPhone);
        userDateOfBirth = (TextView) findViewById(R.id.textDateOfBirth);
        userYear = (Spinner) findViewById(R.id.year_spinner);
        userRegistryNumber = (EditText) findViewById(R.id.editTextRegistryNumber);
        userStudentNumber = (EditText) findViewById(R.id.editTextStudentNumber);
        final String name = userName.getText().toString().trim();
        final String email = userMail.getText().toString().trim();
        final String password = userPassword.getText().toString().trim();
        final String surname = userSurname.getText().toString().trim();
        final String university = "Kocaeli Üniversitesi";
        final String faculty = userFaculty.getSelectedItem().toString().trim();
        final String major = userMajor.getSelectedItem().toString().trim();
        final String address = userAddress.getText().toString().trim();
        final String phoneNumber = userPhoneNumber.getText().toString().trim();
        final String dateOfBirth = userDateOfBirth.getText().toString().trim();
        final Integer year = Integer.parseInt(userYear.getSelectedItem().toString());
        final Long registryNumber = Long.parseLong(userRegistryNumber.getText().toString());
        final Integer studentNumber = Integer.parseInt(userStudentNumber.getText().toString());
        user = new User(
                email,
                name,
                surname,
                password,
                university,
                faculty,
                major,
                address,
                phoneNumber,
                dateOfBirth,
                year,
                registryNumber,
                studentNumber
        );
        // if (validateUserInputs() == false) return;
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String useruid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("Users").child(useruid)
                            .setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        StorageReference storageRef = storage.getReference();
                                        StorageReference userPhotosRef = storageRef.child("user_images").child(useruid);
                                        uploadTask = userPhotosRef.putFile(selectedImageUri);
                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                Toast.makeText(MainActivity.this, "Kayıt başarısız!", Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Toast.makeText(MainActivity.this, "Kayıt başarılı!", Toast.LENGTH_LONG).show();
                                                setContentView(R.layout.application_selection_screen);
                                            }
                                        });
                                    } else {
                                        Toast.makeText(MainActivity.this, "Kayıt başarısız!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void signoutButtonClick(View view) {
        mAuth.getInstance().signOut();
        signInRedirect(this.getWindow().getDecorView().findViewById(android.R.id.content));
    }

    public void sendApplication(View view) {
        Uri transkript_uri, signed_uri, puan_uri;
        DateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        String date = df.format(Calendar.getInstance().getTime());
        StorageReference storageRef = storage.getReference();
        String user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        StorageReference userApplicationRef = storageRef.child("applications").child("cap_applications").child(user_uid + date);
        userApplicationRef.child("transkript.pdf").putFile(cap_transkript_file);
        userApplicationRef.child("puan.pdf").putFile(cap_puan_file);
        userApplicationRef.child("signed.pdf").putFile(cap_signed_pdf);
        Basvuru basvuru = new Basvuru("Çap", date,FirebaseDatabase.getInstance().getReference().child("Users").child(user_uid).child("name").toString(), "Bekliyor");
        FirebaseDatabase.getInstance().getReference().child("Basvurular").child("Çap").child(user_uid + date).setValue(basvuru);
    }

    public void setViewToComplete(View view) {
        EditText cap_faculty_edit = findViewById(R.id.cap_faculty);
        cap_faculty_text = cap_faculty_edit.getText().toString().trim();
        currentView = "Başvuru";
        setContentView(R.layout.send_application);
    }
    public void setApplicationScreen (View view) {
        application_type = findViewById(R.id.application_types);
        String selected_application = application_type.getSelectedItem().toString();
        if (selected_application.equals("Yatay Geçiş")) {
            setContentView(R.layout.yataygecis_screen);
            currentView = "Yatay Geçiş";
        }
        else if (selected_application.equals("Dikey Geçiş")) {
            setContentView(R.layout.dgs_screen);
            currentView = "Dikey Geçiş";
        }
        else if (selected_application.equals("Ders İntibakı")) {
            setContentView(R.layout.dgs_intibak_screen);
            currentView = "Ders İntibakı";
        }
        else if (selected_application.equals("Yaz Okulu")) {
            setContentView(R.layout.yaz_okulu_screen);
            currentView = "Yaz Okulu";
        }
        else if (selected_application.equals("ÇAP")) {
            setContentView(R.layout.cap_screen);
            currentView = "ÇAP";
        }
        else {
            Log.d("APLİKASYON", selected_application);
            return;
        }
    }

    public void printPdfCap(View view) {
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    PdfDocument myPdfDocument = new PdfDocument();
                    Paint myPaint = new Paint();
                    Paint titlePaint = new Paint();

                    PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(1080, 3010, 1).create();
                    PdfDocument.Page myPage1 = myPdfDocument.startPage(myPageInfo1);
                    Canvas canvas = myPage1.getCanvas();
                    String.valueOf(task.getResult().getValue());
                    titlePaint.setTextAlign(Paint.Align.RIGHT);
                    titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    titlePaint.setTextSize(20);
                    canvas.drawText("OGR-F-5", 860, 270, titlePaint);

                    myPaint.setTextAlign(Paint.Align.CENTER);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    myPaint.setTextSize(30);
                    canvas.drawText( "KOCAELİ ÜNİVERSİTESİ", 560, 370, myPaint);

                    myPaint.setTextAlign(Paint.Align.CENTER);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    myPaint.setTextSize(30);
                    canvas.drawText(""+task.getResult().child("major").getValue().toString()+" Bölüm/Program Başkanlığı’na", 560, 500, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("Üniversiteniz "+task.getResult().child("faculty").getValue().toString()+" Fakültesi ", 120, 700, myPaint);


                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText(task.getResult().child("major").getValue().toString()+" Bölümü/Programı (I. Öğr / II. Öğr.) " +task.getResult().child("major").getValue().toString()+" numaralı, " , 70, 800, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText(task.getResult().child("name").getValue().toString() +" isimli öğrencisiyim.", 70, 900, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("Kocaeli Üniversitesi Ön Lisans ve Lisans Eğitim ve Öğretim Yönetmeliği’nin 45. maddesi ", 120, 1000, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("uyarınca, Fakülteniz "+"YENİ_BOLUM"+" Bölümü’nde Çift Anadal Programı (ÇAP)", 70, 1100, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("kapsamında öğrenim görme talebimin kabul edilmesini", 70, 1200, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("Gereği için arz ederim.", 120, 1300, myPaint);

                    myPaint.setTextAlign(Paint.Align.RIGHT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("İmza", 860, 1350, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("Adres: "+task.getResult().child("address").getValue().toString()+"", 70, 1450, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("GSM: "+task.getResult().child("phoneNumber").getValue().toString()+"", 70, 1550, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("E-mail: "+task.getResult().child("email").getValue().toString()+"", 70, 1650, myPaint);

                    myPaint.setTextAlign(Paint.Align.LEFT);
                    myPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                    myPaint.setTextSize(30);
                    canvas.drawText("Eki: Transkript", 70, 1750, myPaint);


                    myPdfDocument.finishPage(myPage1);

                    File file = new File(getApplicationContext().getDataDir().getPath()+"/deneme.pdf");

                    try {
                        myPdfDocument.writeTo(new FileOutputStream(file));
                        Log.e("DOSYA",getApplicationContext().getDataDir().getPath());
                        Toast.makeText(getApplicationContext(), "PDF Başarıyla oluşturuldu", Toast.LENGTH_SHORT).show();
                    } catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "PDF Oluşturulamadı", Toast.LENGTH_SHORT).show();
                    }
                    myPdfDocument.close();
                }
            }
        });



    }
    public boolean validateUserInputs() {
        if (this.userMail == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mAuth.getCurrentUser() != null) {
            // Handle the already signed in user scenario.
            if (mAuth.getCurrentUser().getUid().equals("oMmeOioNtMVgYAb3uRNTCaPmLid2")) {
                setContentView(R.layout.admin_grid);
                listView = findViewById(R.id.basvuru_grid);

                final ArrayList<String> list = new ArrayList<>();
                final ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,list);


                // My top posts by number of stars
                mDatabase.child("Basvurular").child("Çap").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            String value = postSnapshot.getValue(String.class);
                            list.add(value);
                            listView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            setContentView(R.layout.application_selection_screen);
            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1432);
            }
        } else {
            signInRedirect(this.getWindow().getDecorView().findViewById(android.R.id.content));
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}