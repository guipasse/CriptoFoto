package tfm.uoc.edu.criptofoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import tfm.uoc.edu.criptofoto.constants.MainConstants;
import tfm.uoc.edu.criptofoto.constants.MainConstants.GeneralConstants;
import tfm.uoc.edu.criptofoto.database.DatabaseSecurityManager;
import tfm.uoc.edu.criptofoto.model.ImageItem;
import tfm.uoc.edu.criptofoto.model.IntrusionRegisterItem;
import tfm.uoc.edu.criptofoto.model.RepoItem;
import tfm.uoc.edu.criptofoto.security.SecurityManager;

import android.support.constraint.ConstraintLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.Manifest;
import android.content.pm.PackageManager;

import net.sqlcipher.database.SQLiteDatabase;
import android.widget.AdapterView.OnItemSelectedListener;

import com.amnix.materiallockview.MaterialLockView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnItemSelectedListener {

    private Menu menu = null;
    private Toast toast;
    @NonNull
    private Context context;

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private File picturesdirectory;

    private DatabaseSecurityManager dbm;
    private SecurityManager sm;

    private MaterialLockView materialLockViewLogin;
    private MaterialLockView materialLockViewNewUser;
    private MaterialLockView materialLockViewNewRepo;
    private MaterialLockView materialLockViewEditRepo;
    private MaterialLockView materialLockViewSelectRepo;

    private String imagePath;
    private Integer dataIndex;

    private String symetricKey;

    public Uri imguri;

    private String  userPasswordAux, passwordTypeAux, userNameAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Load SQLiteDatabase security libs
            SQLiteDatabase.loadLibs(this);

            // Toolbar implementation
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            /*
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
            */

            // DrawerLayout implementation
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            // NavigationView implementation
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            // Create the security manager instance
            sm = new SecurityManager();

            // Create the database security manager instance
            dbm = new DatabaseSecurityManager();

            // If already registered show always gallery
            if(GeneralConstants.userRegistered){
                hideAllLayouts();
                ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                reloadGalleryData();
                if(((TextView) findViewById(R.id.textViewUserName))!=null){
                    ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                }
                if(((TextView) findViewById(R.id.textViewContainerName))!=null){
                    ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                }
            }

            // DESCOMENTAR SOLS PER TEST
            /*
            getDatabasePath(GeneralConstants.databaseName).delete();
            String path = Environment.getExternalStorageDirectory().getPath().toString() + "/CriptoFoto"; // /.default pumnvkfsoy "/CriptoFoto/.images"
            File gallery= new File(path);
            if(gallery.exists()){
                deleteRecursive(gallery);
                //gallery.delete();
            }
            */

            GeneralConstants.userCreated = dbm.checkDatabaseFileExists(getDatabasePath(GeneralConstants.databaseName));

            // Load context
            context = getApplicationContext();

            checkReadInternalStoragePermission();

            checkWriteExternalStoragePermission();

            checkCameraPermission();


        }catch(Throwable e){
            System.out.println(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GeneralConstants.cameraPermission: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case GeneralConstants.readExternalStoragePermission: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
            case GeneralConstants.writeExternalStoragePermission: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if(GeneralConstants.userRegistered){
            if(((TextView) findViewById(R.id.textViewUserName))!=null){
                ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
            }
            if(((TextView) findViewById(R.id.textViewContainerName))!=null){
                ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        RepoItem repoItem = null;
        try{
            repoItem = (RepoItem) parent.getItemAtPosition(position);
            GeneralConstants.selectedRepositoriIdCombo = repoItem.getId();

            if(repoItem.getKeyType().equals("1")){
                ((EditText) findViewById(R.id.repoSelPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordText)).setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setVisibility(View.INVISIBLE);

                ((Button) findViewById(R.id.buttonSelectRepo)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.keySelectRepoText)).setVisibility(View.VISIBLE);
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).setVisibility(View.INVISIBLE);
            }

            if(repoItem.getKeyType().equals("2")){
                ((EditText) findViewById(R.id.repoSelPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordText)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setVisibility(View.VISIBLE);

                ((Button) findViewById(R.id.buttonSelectRepo)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.keySelectRepoText)).setVisibility(View.VISIBLE);
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).setVisibility(View.INVISIBLE);
            }

            if(repoItem.getKeyType().equals("3")){
                ((EditText) findViewById(R.id.repoSelPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordText)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setVisibility(View.INVISIBLE);

                ((Button) findViewById(R.id.buttonSelectRepo)).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.keySelectRepoText)).setVisibility(View.INVISIBLE);
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).setVisibility(View.VISIBLE);
            }

        }catch(Throwable e){
            System.out.println(e);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        reloadMainMenu();
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            if (requestCode == GeneralConstants.requestImageCapture && resultCode == RESULT_OK) {
                String imgpath = imguri.getEncodedPath();
                if (imguri != null) {
                    Bitmap imageBitmap = BitmapFactory.decodeFile(imgpath);
                    new File(imgpath).delete();
                    sm.saveEncryptedImage(Base64.decode(GeneralConstants.selectedRepositoriIV, Base64.DEFAULT), imageBitmap, imgpath, Base64.decode(GeneralConstants.selectedRepositoriCryptoKey, Base64.DEFAULT));
                    reloadGalleryData();
                }
            }
            if(requestCode == GeneralConstants.requestImageDeleteAndClose && resultCode == RESULT_OK) {
                if(GeneralConstants.userRegistered){
                    hideAllLayouts();
                    ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                    reloadGalleryData();
                    if(((TextView) findViewById(R.id.textViewUserName))!=null){
                        ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                    }
                    if(((TextView) findViewById(R.id.textViewContainerName))!=null){
                        ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ConstraintLayout layout;
        try {
            hideAllLayouts();
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            if (id == R.id.nav_login) {
                layout = (ConstraintLayout) findViewById(R.id.content_login);
                layout.setVisibility(View.VISIBLE);
                loadPatternLogin();
            } else if (id == R.id.nav_gallery) {
                RelativeLayout layoutGallery = (RelativeLayout) findViewById(R.id.content_gallery);
                layoutGallery.setVisibility(View.VISIBLE);
                reloadGalleryData();
            }else if (id == R.id.nav_selectrepo) {
                layout = (ConstraintLayout) findViewById(R.id.content_selectrepo);
                layout.setVisibility(View.VISIBLE);
                loadPatternSelectRepo();
                loadRepoCombo();
            } else if (id == R.id.nav_newuser) {
                layout = (ConstraintLayout) findViewById(R.id.content_newuser);
                layout.setVisibility(View.VISIBLE);
                loadPatternNewUser();
            } else if (id == R.id.nav_edituser) {
                layout = (ConstraintLayout) findViewById(R.id.content_edituser);
                layout.setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.userEditNameText)).setText(GeneralConstants.currentUserName);
            } else if (id == R.id.nav_intrusionregister) {
                layout = (ConstraintLayout) findViewById(R.id.content_intrusionregister);
                layout.setVisibility(View.VISIBLE);
                loadIntrusionRegister();
            } else if (id == R.id.nav_newrepo) {
                layout = (ConstraintLayout) findViewById(R.id.content_newrepo);
                layout.setVisibility(View.VISIBLE);
                loadPatternNewRepo();
            } else if (id == R.id.nav_editrepo) {
                layout = (ConstraintLayout) findViewById(R.id.content_editrepo);
                layout.setVisibility(View.VISIBLE);
                loadPatternEditRepo();
                loadEditRepoData();
            } else if (id == R.id.nav_exit_session) {
                cleanAndExit();
            } else if (id == R.id.nav_exit) {
                finishAndRemoveTask();
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } catch (Throwable e) {
            System.out.println(e);
            hideAllLayouts();
            ConstraintLayout mainlayout = (ConstraintLayout) findViewById(R.id.content_main);
            mainlayout.setVisibility(View.VISIBLE);
        }
        return true;
    }

    private void cleanAndExit(){
        try{
            GeneralConstants.userRegistered = false;
            GeneralConstants.currentUserName = "";
            GeneralConstants.selectedRepositoriName = "";
            GeneralConstants.selectedRepositoriCryptoKey = "";
            GeneralConstants.selectedRepositoriIV = "";
            GeneralConstants.selectedRepositoriTypeKey = new Integer(0);
            GeneralConstants.selectedRepositoriId = new Integer(0);
            GeneralConstants.selectedRepositoriKey = "";
            GeneralConstants.selectedRepositoriPath = "";
            GeneralConstants.selectedRepositoriDefault = new Integer(0);
            clickLoginKeyTextType(null);
            ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.INVISIBLE);
            reloadMainMenu();
        }catch(Throwable e){
            System.out.println(e);
        }
    }

    private void hideAllLayouts(){
        try{
            ((ConstraintLayout) findViewById(R.id.content_main)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_editrepo)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_edituser)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_intrusionregister)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_login)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_newrepo)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_newuser)).setVisibility(View.INVISIBLE);
            ((ConstraintLayout) findViewById(R.id.content_selectrepo)).setVisibility(View.INVISIBLE);
            ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.INVISIBLE);
        }catch(Throwable e){
            System.out.println(e);
        }
    }

    private void reloadMainMenu(){
        try{
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            MenuItem selectrepo = navigationView.getMenu().findItem(R.id.nav_selectrepo);
            MenuItem gallery = navigationView.getMenu().findItem(R.id.nav_gallery);
            MenuItem edituser = navigationView.getMenu().findItem(R.id.nav_edituser);
            MenuItem intrusionregister = navigationView.getMenu().findItem(R.id.nav_intrusionregister);
            MenuItem newrepo = navigationView.getMenu().findItem(R.id.nav_newrepo);
            MenuItem editrepo = navigationView.getMenu().findItem(R.id.nav_editrepo);
            MenuItem repogroup = navigationView.getMenu().findItem(R.id.nav_repo_group);
            MenuItem newuser = navigationView.getMenu().findItem(R.id.nav_newuser);
            MenuItem login = navigationView.getMenu().findItem(R.id.nav_login);
            MenuItem usergroup = navigationView.getMenu().findItem(R.id.nav_user_group);
            MenuItem exitsession = navigationView.getMenu().findItem(R.id.nav_exit_session);
            MenuItem exitapp = navigationView.getMenu().findItem(R.id.nav_exit);
            if(GeneralConstants.userCreated){
                login.setVisible(true);
                newuser.setVisible(false);
                usergroup.setVisible(false);
                exitapp.setVisible(true);
            }else{
                login.setVisible(false);
                newuser.setVisible(true);
                usergroup.setVisible(true);
                exitapp.setVisible(true);
            }
            if(!GeneralConstants.userRegistered){
                selectrepo.setVisible(false);
                gallery.setVisible(false);
                edituser.setVisible(false);
                intrusionregister.setVisible(false);
                newrepo.setVisible(false);
                editrepo.setVisible(false);
                repogroup.setVisible(false);
                exitsession.setVisible(false);
            }else{
                selectrepo.setVisible(true);
                gallery.setVisible(true);
                edituser.setVisible(true);
                intrusionregister.setVisible(true);
                newrepo.setVisible(true);
                editrepo.setVisible(true);
                repogroup.setVisible(true);
                login.setVisible(false);
                usergroup.setVisible(true);
                exitsession.setVisible(true);
                exitapp.setVisible(false);
            }
        }catch(Throwable e){
            System.out.println(e);
        }
    }

    public void createRepo(View v) {
        Editable repoPassword = null;
        Boolean validation = true;
        Integer passwordType = 1;
        Editable repoName = null;
        CheckBox defecte = null;
        try {
            // Get the user data and validate
            repoName = ((EditText) findViewById(R.id.repoNameText)).getText();
            if(repoName.toString().equals("") || repoName.toString().length()<4){
                validation = false;
                Toast.makeText(context, "El nom del repositori ha de tindre algun valor superior a 3 lletres", Toast.LENGTH_LONG).show();
            }

            if(((RadioButton) findViewById(R.id.radioRepoButtonTextKeyType)).isChecked()){
                repoPassword = ((EditText) findViewById(R.id.repoPasswordText)).getText();
                passwordType = 1;
                if(repoPassword.toString().equals("") || repoPassword.toString().length()<5){
                    validation = false;
                    Toast.makeText(context, "La clau del repositori ha de tindre algun valor superior a 4 lletres", Toast.LENGTH_LONG).show();
                }
            }

            if(((RadioButton) findViewById(R.id.radioRepoButtonPINKeyType)).isChecked()){
                repoPassword = ((EditText) findViewById(R.id.repoPasswordPIN)).getText();
                passwordType = 2;
                if(repoPassword.toString().equals("") || repoPassword.toString().length()<5){
                    validation = false;
                    Toast.makeText(context, "La clau del repositori ha de tindre algun valor superior a 4 números", Toast.LENGTH_LONG).show();
                }
            }

            defecte = ((CheckBox) findViewById(R.id.checkBoxRepoDefault));

            if(validation){
                createRepoCore(repoPassword.toString(), passwordType.toString(), repoName.toString(), defecte.isChecked());
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void createRepoCore(String repoPassword, String passwordType, String repoName, Boolean defecte) {
        Boolean validation = true;
        try {
            validation = dbm.createRepo(repoPassword, passwordType, repoName, defecte);
            if(validation){
                // Change interface
                reloadMainMenu();
                hideAllLayouts();
                ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                reloadGalleryData();
                Toast.makeText(context, "El repositori s'ha creat correctament i ja s'hi pot treballar sobre ell.", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, "El repositori no s'ha creat correctament. Per favor, torneu a provar.", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            System.out.println(e);
            Toast.makeText(context, "El repositori no s'ha creat correctament. Per favor, torneu a provar.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPatternNewRepo(){
        materialLockViewNewRepo = null;
        try{
            materialLockViewNewRepo = (MaterialLockView) findViewById(R.id.patternNewRepo);
            materialLockViewNewRepo.clearPattern();
            materialLockViewNewRepo.setInStealthMode(false);
            materialLockViewNewRepo.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                @Override
                public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                    Editable repoName = ((EditText) findViewById(R.id.repoNameText)).getText();
                    CheckBox defecte = ((CheckBox) findViewById(R.id.checkBoxRepoDefault));
                    createRepoCore(SimplePattern, "3", repoName.toString(), defecte.isChecked());
                    super.onPatternDetected(pattern, SimplePattern);
                }
            });
            materialLockViewNewRepo.setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickRepoKeyTextType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioRepoButtonTextKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioRepoButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.repoPasswordText)).setText("");
            ((EditText) findViewById(R.id.repoPasswordText)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.repoPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.repoPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttonCreateRepo)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyNewRepoText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternNewRepo)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternNewRepo)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickRepoKeyPINType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioRepoButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoButtonPINKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioRepoButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.repoPasswordText)).setText("");
            ((EditText) findViewById(R.id.repoPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.repoPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.repoPasswordPIN)).setVisibility(View.VISIBLE);

            ((Button) findViewById(R.id.buttonCreateRepo)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyNewRepoText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternNewRepo)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternNewRepo)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickRepoKeyPatternType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioRepoButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoButtonPatternKeyType)).setChecked(true);
            ((EditText) findViewById(R.id.repoPasswordText)).setText("");
            ((EditText) findViewById(R.id.repoPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.repoPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.repoPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttonCreateRepo)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.keyNewRepoText)).setVisibility(View.INVISIBLE);
            ((MaterialLockView) findViewById(R.id.patternNewRepo)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternNewRepo)).setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void editRepo(View v) {
        Editable repoPassword = null;
        Boolean validation = true;
        Integer passwordType = 1;
        Editable repoName = null;
        CheckBox defecte = null;
        try {
            // Get the user data and validate
            repoName = ((EditText) findViewById(R.id.repoEditNameText)).getText();
            if(repoName.toString().equals("") || repoName.toString().length()<4){
                validation = false;
                Toast.makeText(context, "El nom del repositori ha de tindre algun valor superior a 3 lletres", Toast.LENGTH_LONG).show();
            }

            if(((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).isChecked()){
                repoPassword = ((EditText) findViewById(R.id.repoEditPasswordText)).getText();
                passwordType = 1;
                if(repoPassword.toString().equals("") || repoPassword.toString().length()<5){
                    validation = false;
                    Toast.makeText(context, "La clau del repositori ha de tindre algun valor superior a 4 lletres", Toast.LENGTH_LONG).show();
                }
            }

            if(((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).isChecked()){
                repoPassword = ((EditText) findViewById(R.id.repoEditPasswordPIN)).getText();
                passwordType = 2;
                if(repoPassword.toString().equals("") || repoPassword.toString().length()<5){
                    validation = false;
                    Toast.makeText(context, "La clau del repositori ha de tindre algun valor superior a 4 números", Toast.LENGTH_LONG).show();
                }
            }

            defecte = ((CheckBox) findViewById(R.id.checkBoxRepoEditDefault));

            if(validation){
                editRepoCore(repoPassword.toString(), passwordType.toString(), repoName.toString(), defecte.isChecked());
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void editRepoCore(String repoPassword, String passwordType, String repoName, Boolean defecte) {
        Boolean validation = true;
        try {
            validation = dbm.editRepo(repoPassword, passwordType, repoName, defecte);
            if(validation){
                // Change interface
                reloadMainMenu();
                hideAllLayouts();
                ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                Toast.makeText(context, "El repositori s'ha modificat correctament i ja s'hi pot treballar sobre ell.", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, "El repositori no s'ha modificat correctament. Per favor, torneu a provar.", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            System.out.println(e);
            Toast.makeText(context, "El repositori no s'ha modificat correctament. Per favor, torneu a provar.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPatternEditRepo(){
        materialLockViewEditRepo = null;
        try{
            materialLockViewEditRepo = (MaterialLockView) findViewById(R.id.patternEditRepo);
            materialLockViewEditRepo.clearPattern();
            materialLockViewEditRepo.setInStealthMode(false);
            materialLockViewEditRepo.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                @Override
                public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                    Editable repoName = ((EditText) findViewById(R.id.repoEditNameText)).getText();
                    CheckBox defecte = ((CheckBox) findViewById(R.id.checkBoxRepoEditDefault));
                    editRepoCore(SimplePattern, "3", repoName.toString(), defecte.isChecked());
                    super.onPatternDetected(pattern, SimplePattern);
                }
            });
            materialLockViewEditRepo.setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void loadEditRepoData(){
        try{
            ((EditText) findViewById(R.id.repoEditNameText)).setText(GeneralConstants.selectedRepositoriName);
            if(GeneralConstants.selectedRepositoriTypeKey==1){
                ((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).setChecked(true);
                ((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).setChecked(false);
                ((RadioButton) findViewById(R.id.radioRepoEditButtonPatternKeyType)).setChecked(false);
                ((EditText) findViewById(R.id.repoEditPasswordText)).setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoEditPasswordText)).setText(GeneralConstants.selectedRepositoriKey);

                ((Button) findViewById(R.id.buttonEditRepo)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.keyEditRepoText)).setVisibility(View.VISIBLE);
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).setVisibility(View.INVISIBLE);
            }
            if(GeneralConstants.selectedRepositoriTypeKey==2){
                ((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).setChecked(false);
                ((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).setChecked(true);
                ((RadioButton) findViewById(R.id.radioRepoEditButtonPatternKeyType)).setChecked(false);
                ((EditText) findViewById(R.id.repoEditPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoEditPasswordText)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText(GeneralConstants.selectedRepositoriKey);

                ((Button) findViewById(R.id.buttonEditRepo)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.keyEditRepoText)).setVisibility(View.VISIBLE);
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).setVisibility(View.INVISIBLE);
            }
            if(GeneralConstants.selectedRepositoriTypeKey==3){
                ((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).setChecked(false);
                ((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).setChecked(false);
                ((RadioButton) findViewById(R.id.radioRepoEditButtonPatternKeyType)).setChecked(true);
                ((EditText) findViewById(R.id.repoEditPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoEditPasswordText)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText(GeneralConstants.selectedRepositoriKey);

                ((Button) findViewById(R.id.buttonEditRepo)).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.keyEditRepoText)).setVisibility(View.INVISIBLE);
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).setVisibility(View.VISIBLE);

                ArrayList<MaterialLockView.Cell> mPattern = new ArrayList<>(GeneralConstants.selectedRepositoriKey.length());
                char[] array = GeneralConstants.selectedRepositoriKey.toCharArray();
                MaterialLockView.Cell aux = null;
                for (char item : array) {
                    aux = getCellFromNumber(new Integer(String.valueOf(item)));
                    mPattern.add(aux);
                }
                ((MaterialLockView) findViewById(R.id.patternEditRepo)).setPattern(MaterialLockView.DisplayMode.Correct, mPattern);
            }
            if(GeneralConstants.selectedRepositoriDefault==1){
                ((CheckBox) findViewById(R.id.checkBoxRepoEditDefault)).setChecked(true);
            }else{
                ((CheckBox) findViewById(R.id.checkBoxRepoEditDefault)).setChecked(false);
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private MaterialLockView.Cell getCellFromNumber(Integer position) {
        if (position == null)
            return null;
        switch (position) {
            case 1:
                return MaterialLockView.Cell.of(0, 0);
            case 2:
                return MaterialLockView.Cell.of(0, 1);
            case 3:
                return MaterialLockView.Cell.of(0, 2);
            case 4:
                return MaterialLockView.Cell.of(1, 0);
            case 5:
                return MaterialLockView.Cell.of(1, 1);
            case 6:
                return MaterialLockView.Cell.of(1, 2);
            case 7:
                return MaterialLockView.Cell.of(2, 0);
            case 8:
                return MaterialLockView.Cell.of(2, 1);
            case 9:
                return MaterialLockView.Cell.of(2, 2);
        }
        return null;
    }

    public void clickRepoEditKeyTextType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoEditButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.repoEditPasswordText)).setText("");
            ((EditText) findViewById(R.id.repoEditPasswordText)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.repoEditPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttonEditRepo)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyEditRepoText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternEditRepo)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternEditRepo)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickRepoEditKeyPINType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioRepoEditButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.repoEditPasswordText)).setText("");
            ((EditText) findViewById(R.id.repoEditPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.repoEditPasswordPIN)).setVisibility(View.VISIBLE);

            ((Button) findViewById(R.id.buttonEditRepo)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyEditRepoText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternEditRepo)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternEditRepo)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickRepoEditKeyPatternType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioRepoEditButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoEditButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioRepoEditButtonPatternKeyType)).setChecked(true);
            ((EditText) findViewById(R.id.repoEditPasswordText)).setText("");
            ((EditText) findViewById(R.id.repoEditPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.repoEditPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.repoEditPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttonEditRepo)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.keyEditRepoText)).setVisibility(View.INVISIBLE);
            ((MaterialLockView) findViewById(R.id.patternEditRepo)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternEditRepo)).setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void autenticateUser(View v) {
        try{
            autenticateUserCore(getLoginUserKey());
        }catch(Throwable e){
            System.out.println(e);
        }
    }

    private void autenticateUserCore(String userKey) {
        Boolean result = false;
        try{
            result = dbm.autenticateUser(getDatabasePath(GeneralConstants.databaseName), userKey);
            if(result){
                materialLockViewLogin.setDisplayMode(MaterialLockView.DisplayMode.Correct);
                GeneralConstants.userRegistered = true;
                GeneralConstants.userCreated = true;
                reloadMainMenu();
                hideAllLayouts();
                ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                reloadGalleryData();
            }else{
               materialLockViewLogin.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
               Toast.makeText(context, "L'usuari no s'ha pogut autenticar correctament, per favor, torni a provar", Toast.LENGTH_LONG).show();
               createIntrusionRegister(userKey);
            }
        }catch(Throwable e){
            materialLockViewLogin.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
            System.out.println(e);
            Toast.makeText(context, "L'usuari no s'ha pogut autenticar correctament, per favor, torni a provar", Toast.LENGTH_LONG).show();
            createIntrusionRegister(userKey);
        }
    }

    private void createIntrusionRegister(String userKey){
        try{
            if(sm.getPublicKey()!=null){
                DateFormat dateFormatData = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat dateFormatHour = new SimpleDateFormat("HH:mm:ss");
                String data = dateFormatData.format(new Date()).toString();
                String hora = dateFormatHour.format(new Date()).toString();
                String key = userKey;

                imagePath = Environment.getExternalStorageDirectory().getPath().toString() + GeneralConstants.intrusionRegisterImagesPath;
                File aux = new File(imagePath);
                if (!aux.exists()) {
                    if (!aux.mkdirs()) {
                        Toast.makeText(context, "Error al crear el directori del registre d'intrusions", Toast.LENGTH_LONG).show();
                    }
                }

                String dataPath = Environment.getExternalStorageDirectory().getPath().toString() + GeneralConstants.intrusionRegisterDataPath;
                aux = new File(dataPath);
                if (!aux.exists()) {
                    if (!aux.mkdirs()) {
                        Toast.makeText(context, "Error al crear el directori del registre d'intrusions", Toast.LENGTH_LONG).show();
                    }
                }
                dataIndex = aux.listFiles().length+1;
                symetricKey = sm.generateAESKey();
                try{
                    PrintWriter writer = new PrintWriter(dataPath + "." + dataIndex + ".dat", "UTF-8");
                    String text = sm.encryptTextRSA(data+";"+hora+";"+key+";"+symetricKey, sm.getPublicKey());
                    writer.println(text);
                    writer.close();
                } catch (IOException io) {
                    System.out.println(io);
                }
                takePhoto();
            }
        }catch(Throwable ex){
            System.out.println(ex);
        }
    }

    private void takePhoto(){
        Camera mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        try{
            try {
                mCamera.setPreviewTexture(new SurfaceTexture(123));
            } catch (IOException e1) {
                System.out.println(e1);
            }
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureFormat(ImageFormat.JPEG);
            mCamera.setParameters(params);
            mCamera.startPreview();
            mCamera.takePicture(null, null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try{
                        String imagePathFinal = imagePath + "." + dataIndex + ".jpg";
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0 ,data.length);
                        sm.saveEncryptedImage(GeneralConstants.ivBytesIntrusionImages, bmp, imagePathFinal, Base64.decode(symetricKey, Base64.DEFAULT));
                        camera.stopPreview();
                        camera.release();
                    } catch (Throwable iox) {
                        System.out.println(iox);
                        camera.stopPreview();
                        camera.release();
                    }
                }
            });
        } catch (Throwable e) {
            System.out.println(e);
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    private void loadPatternLogin(){
        materialLockViewLogin = null;
        try{
            materialLockViewLogin = (MaterialLockView) findViewById(R.id.patternLogin);
            materialLockViewLogin.clearPattern();
            materialLockViewLogin.setInStealthMode(false);
            materialLockViewLogin.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                @Override
                public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                    autenticateUserCore(SimplePattern);
                    super.onPatternDetected(pattern, SimplePattern);
                }
            });
            materialLockViewLogin.setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public String getLoginUserKey() {
        try {
            if(((RadioButton) findViewById(R.id.radioLoginButtonTextKeyType)).isChecked()){
                return ((EditText) findViewById(R.id.loginPasswordText)).getText().toString();
            }
            if(((RadioButton) findViewById(R.id.radioLoginButtonPINKeyType)).isChecked()){
                return ((EditText) findViewById(R.id.loginPasswordPIN)).getText().toString();
            }
            if(((RadioButton) findViewById(R.id.radioLoginButtonPatternKeyType)).isChecked()){
                return "";
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
        return "";
    }

    public void clickLoginKeyTextType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioLoginButtonTextKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioLoginButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioLoginButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.loginPasswordText)).setText("");
            ((EditText) findViewById(R.id.loginPasswordText)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.loginPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.loginPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttonlogin)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyLoginText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternLogin)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternLogin)).setVisibility(View.INVISIBLE);;
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickLoginKeyPINType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioLoginButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioLoginButtonPINKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioLoginButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.loginPasswordText)).setText("");
            ((EditText) findViewById(R.id.loginPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.loginPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.loginPasswordPIN)).setVisibility(View.VISIBLE);

            ((Button) findViewById(R.id.buttonlogin)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyLoginText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternLogin)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternLogin)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickLoginKeyPatternType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioLoginButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioLoginButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioLoginButtonPatternKeyType)).setChecked(true);
            ((EditText) findViewById(R.id.loginPasswordText)).setText("");
            ((EditText) findViewById(R.id.loginPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.loginPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.loginPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttonlogin)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.keyLoginText)).setVisibility(View.INVISIBLE);
            ((MaterialLockView) findViewById(R.id.patternLogin)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternLogin)).setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void createUser(View v) {
        Boolean validation = true;
        Editable userName = null;
        Editable userPassword = null;
        Integer passwordType = 1;
        try {
            // Get the user data and validate
            userName = ((EditText) findViewById(R.id.userNameText)).getText();
            if(userName.toString().equals("") || userName.toString().length()<5){
                validation = false;
                Toast.makeText(context, "El nom de l'usuari ha de tindre algun valor superior a 4 lletres", Toast.LENGTH_LONG).show();
            }

            if(((RadioButton) findViewById(R.id.radioButtonTextKeyType)).isChecked()){
                userPassword = ((EditText) findViewById(R.id.userPasswordText)).getText();
                passwordType = 1;
                if(userPassword.toString().equals("") || userPassword.toString().length()<5){
                    validation = false;
                    Toast.makeText(context, "La clau de l'usuari ha de tindre algun valor superior a 4 lletres", Toast.LENGTH_LONG).show();
                }
            }

            if(((RadioButton) findViewById(R.id.radioButtonPINKeyType)).isChecked()){
                userPassword = ((EditText) findViewById(R.id.userPasswordPIN)).getText();
                passwordType = 2;
                if(userPassword.toString().equals("") || userPassword.toString().length()<5){
                    validation = false;
                    Toast.makeText(context, "La clau de l'usuari ha de tindre algun valor superior a 4 números", Toast.LENGTH_LONG).show();
                }
            }

            if(validation){
                createUserCore(userPassword.toString(), passwordType.toString(), userName.toString());
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void createUserCore(String userPassword, String passwordType, String userName) {
        try {
            Toast.makeText(context, "L'aplicació està creant els recursos de l'usuari. Per favor, espereu uns segons mentre l'aplicació està processant.", Toast.LENGTH_LONG).show();
            userPasswordAux = userPassword;
            passwordTypeAux = passwordType;
            userNameAux = userName;
            new Thread() {
                @Override
                public void run() {
                    try {
                        Boolean validation = dbm.createUser(getDatabasePath(GeneralConstants.databaseName), userPasswordAux, passwordTypeAux, userNameAux);
                        if(validation){
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    // Change interface
                                    GeneralConstants.userCreated = true;
                                    reloadMainMenu();
                                    hideAllLayouts();
                                    Toast.makeText(context, "L'usuari s'ha creat correctament. Per favor, autentiqueu-se per poder utilitzar l'aplicació.", Toast.LENGTH_LONG).show();
                                    ((ConstraintLayout) findViewById(R.id.content_main)).setVisibility(View.VISIBLE);
                                }
                            });
                        }else{
                            Toast.makeText(context, "S'ha produit un error al crear l'usuari.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Throwable e) {
                        System.out.println(e);
                        Toast.makeText(context, "S'ha produit un error al crear l'usuari.", Toast.LENGTH_LONG).show();
                    }
                }
            }.start();
        } catch (Throwable e) {
            System.out.println(e);
            Toast.makeText(context, "S'ha produit un error al crear l'usuari.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPatternNewUser(){
        materialLockViewNewUser = null;
        try{
            materialLockViewNewUser = (MaterialLockView) findViewById(R.id.patternNewUser);
            materialLockViewNewUser.clearPattern();
            materialLockViewNewUser.setInStealthMode(false);
            materialLockViewNewUser.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                @Override
                public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                    Editable userName = ((EditText) findViewById(R.id.userNameText)).getText();
                    createUserCore(SimplePattern, "3", userName.toString());
                    super.onPatternDetected(pattern, SimplePattern);
                }
            });
            materialLockViewNewUser.setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickUserKeyTextType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioButtonTextKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.userPasswordText)).setText("");
            ((EditText) findViewById(R.id.userPasswordText)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.userPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.userPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttoncreateuser)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyNewUserText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternNewUser)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternNewUser)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickUserKeyPINType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonPINKeyType)).setChecked(true);
            ((RadioButton) findViewById(R.id.radioButtonPatternKeyType)).setChecked(false);
            ((EditText) findViewById(R.id.userPasswordText)).setText("");
            ((EditText) findViewById(R.id.userPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.userPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.userPasswordPIN)).setVisibility(View.VISIBLE);

            ((Button) findViewById(R.id.buttoncreateuser)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.keyNewUserText)).setVisibility(View.VISIBLE);
            ((MaterialLockView) findViewById(R.id.patternNewUser)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternNewUser)).setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void clickUserKeyPatternType(View v) {
        try {
            ((RadioButton) findViewById(R.id.radioButtonTextKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonPINKeyType)).setChecked(false);
            ((RadioButton) findViewById(R.id.radioButtonPatternKeyType)).setChecked(true);
            ((EditText) findViewById(R.id.userPasswordText)).setText("");
            ((EditText) findViewById(R.id.userPasswordText)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.userPasswordPIN)).setText("");
            ((EditText) findViewById(R.id.userPasswordPIN)).setVisibility(View.INVISIBLE);

            ((Button) findViewById(R.id.buttoncreateuser)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.keyNewUserText)).setVisibility(View.INVISIBLE);
            ((MaterialLockView) findViewById(R.id.patternNewUser)).clearPattern();
            ((MaterialLockView) findViewById(R.id.patternNewUser)).setVisibility(View.VISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void editUser(View v) {
        Boolean validation = true;
        Editable userName = null;
        try{
            userName = ((EditText) findViewById(R.id.userEditNameText)).getText();
            if(userName.toString().equals("") || userName.toString().length()<5){
                validation = false;
                Toast.makeText(context, "El nom de l'usuari ha de tindre algun valor superior a 4 lletres", Toast.LENGTH_LONG).show();
            }
            if(validation){
                dbm.editUser(userName.toString());
                reloadMainMenu();
                hideAllLayouts();
                ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                Toast.makeText(context, "L'usuari s'ha actualitzat correctament", Toast.LENGTH_LONG).show();
                reloadGalleryData();
            }
        }catch(Throwable e){
            System.out.println(e);
            Toast.makeText(context, "L'usuari no s'ha pogut actualitzar correctament, per favor, torni a provar", Toast.LENGTH_LONG).show();
        }
    }

    public void selectRepo(View v) {
        RepoItem repoItem = null;
        EditText key = null;
        try{
            repoItem = GeneralConstants.repositoriesForCombo.get(GeneralConstants.selectedRepositoriIdCombo-1);
            if(repoItem.getKeyType().equals("1")){
                key = ((EditText) findViewById(R.id.repoSelPasswordText));
            }
            if(repoItem.getKeyType().equals("2")){
                key = ((EditText) findViewById(R.id.repoSelPasswordPIN));
            }
            selectRepoCore(repoItem, key.getText().toString());
        }catch(Throwable e){
            System.out.println(e);
            Toast.makeText(context, "La contrasenya del repositori no és correcta. Per favor, torneu a provar", Toast.LENGTH_LONG).show();
        }
    }

    public void selectRepoCore(RepoItem repoItem, String key) {
        try{
            if(repoItem.getKey().equals(key)){
                GeneralConstants.selectedRepositoriName = repoItem.getName();
                GeneralConstants.selectedRepositoriIV = repoItem.getIv();
                GeneralConstants.selectedRepositoriId = repoItem.getId();
                GeneralConstants.selectedRepositoriTypeKey = new Integer(repoItem.getKeyType());
                GeneralConstants.selectedRepositoriCryptoKey = repoItem.getCryptoKey();
                GeneralConstants.selectedRepositoriKey = repoItem.getKey();
                GeneralConstants.selectedRepositoriPath = repoItem.getPath();
                GeneralConstants.selectedRepositoriDefault = repoItem.getDef();
                reloadMainMenu();
                hideAllLayouts();
                ((RelativeLayout) findViewById(R.id.content_gallery)).setVisibility(View.VISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                Toast.makeText(context, "La contrasenya es correcta. El repositori s'ha carregat correctament", Toast.LENGTH_LONG).show();
                reloadGalleryData();
            }else{
                Toast.makeText(context, "La contrasenya del repositori no és correcta. Per favor, torneu a provar", Toast.LENGTH_LONG).show();
            }
        }catch(Throwable e){
            System.out.println(e);
            Toast.makeText(context, "La contrasenya del repositori no és correcta. Per favor, torneu a provar", Toast.LENGTH_LONG).show();
        }
    }

    private void loadPatternSelectRepo(){
        materialLockViewSelectRepo = null;
        try{
            materialLockViewSelectRepo = (MaterialLockView) findViewById(R.id.patternSelectRepo);
            materialLockViewSelectRepo.clearPattern();
            materialLockViewSelectRepo.setInStealthMode(false);
            materialLockViewSelectRepo.setOnPatternListener(new MaterialLockView.OnPatternListener() {
                @Override
                public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                    RepoItem repoItem = GeneralConstants.repositoriesForCombo.get(GeneralConstants.selectedRepositoriIdCombo-1);
                    selectRepoCore(repoItem, SimplePattern);
                    super.onPatternDetected(pattern, SimplePattern);
                }
            });
            materialLockViewSelectRepo.setVisibility(View.INVISIBLE);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    public void launchCamera(View v) {
        try {
            checkCameraPermission();
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imguri = createPicturesFile();
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imguri);
            if (intentCamera.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intentCamera, GeneralConstants.requestImageCapture);
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void checkCameraPermission(){
        try {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            GeneralConstants.cameraPermission);
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private Uri createPicturesFile() {
        try{
            String storagestate = Environment.getExternalStorageState();
            if(storagestate.equals(Environment.MEDIA_MOUNTED)){
                checkWriteExternalStoragePermission();
                String path = Environment.getExternalStorageDirectory().getPath().toString()+ GeneralConstants.selectedRepositoriPath ;
                picturesdirectory = new File(path);
                if(!picturesdirectory.exists()){
                    if(!picturesdirectory.mkdirs()){
                        Toast.makeText(context, "Ha fallat la creació del directori del repositori", Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = picturesdirectory.getPath() + File.separator + ".IMG_" + timeStamp + ".jpg";
                File imageFile = new File (filename);
                return Uri.fromFile(imageFile);
            }else{
                return null;
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
        return null;
    }

    private void checkWriteExternalStoragePermission(){
        try {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MainConstants.GeneralConstants.writeExternalStoragePermission);
                }
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void checkReadInternalStoragePermission(){
        try {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            GeneralConstants.readExternalStoragePermission);
                }
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void reloadGalleryData() {
        try{
            gridView = (GridView) findViewById(R.id.gridView);
            gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getRepositoryData());
            gridView.setAdapter(gridAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                    //Create intent
                    Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
                    intent.putExtra("image", item.getImage());
                    intent.putExtra("path", item.getPath());
                    intent.putExtra("filename", item.getTitle());
                    intent.putExtra("rsa", false);
                    //Start details activity
                    startActivity(intent);
                }


            });
            if(GeneralConstants.userRegistered){
                ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                if(((TextView) findViewById(R.id.textViewUserName))!=null){
                    ((TextView) findViewById(R.id.textViewUserName)).setText("Usuari: " + GeneralConstants.currentUserName);
                }
                if(((TextView) findViewById(R.id.textViewContainerName))!=null){
                    ((TextView) findViewById(R.id.textViewContainerName)).setText("Repositori: " + GeneralConstants.selectedRepositoriName);
                }
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private ArrayList<ImageItem> getRepositoryData() {
        try {
            checkReadInternalStoragePermission();
            final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
            if (GeneralConstants.selectedRepositoriPath != null && !GeneralConstants.selectedRepositoriPath.equals("")) {
                String path = Environment.getExternalStorageDirectory().getPath().toString() + GeneralConstants.selectedRepositoriPath;
                File gallery = new File(path);
                if (!gallery.exists()) {
                    if (!gallery.mkdirs()) {
                        Toast.makeText(context, "Fail creating Images Folder", Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
                File[] image = gallery.listFiles();
                for (int i = 0; i < image.length; i++) {
                    try {
                        Bitmap bitmap = sm.loadEncryptedImage(Base64.decode(GeneralConstants.selectedRepositoriIV, Base64.DEFAULT), image[i], Base64.decode(GeneralConstants.selectedRepositoriCryptoKey, Base64.DEFAULT));
                        String filename = image[i].getName();
                        Bitmap bitmap_resize = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                        imageItems.add(new ImageItem(bitmap_resize, filename, path + "/" + filename));
                    } catch (Throwable e) {
                        System.out.println(e);
                    }
                }
            }
            return imageItems;
        } catch (Throwable e) {
            System.out.println(e);
        }
        return new ArrayList<ImageItem>();
    }

    private void loadRepoCombo(){
        try{
            // Spinner element
            Spinner spinner = (Spinner) findViewById(R.id.comboSelRepo);

            // Spinner click listener
            spinner.setOnItemSelectedListener(this);

            // Spinner Drop down elements
            List<RepoItem> repos = dbm.getRepositoriesForCombo();

            // Creating adapter for spinner
            ArrayAdapter<RepoItem> dataAdapter = new ArrayAdapter<RepoItem>(this, android.R.layout.simple_spinner_item, repos);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spinner.setAdapter(dataAdapter);

            spinner.setSelection(0);

            RepoItem repoItem = repos.get(0);

            GeneralConstants.selectedRepositoriIdCombo = repoItem.getId();
            if(repoItem.getKeyType().equals("1")){
                ((EditText) findViewById(R.id.repoSelPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordText)).setVisibility(View.VISIBLE);
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setVisibility(View.INVISIBLE);

                ((Button) findViewById(R.id.buttonSelectRepo)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.keySelectRepoText)).setVisibility(View.VISIBLE);
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).setVisibility(View.INVISIBLE);
            }

            if(repoItem.getKeyType().equals("2")){
                ((EditText) findViewById(R.id.repoSelPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordText)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setVisibility(View.VISIBLE);

                ((Button) findViewById(R.id.buttonSelectRepo)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.keySelectRepoText)).setVisibility(View.VISIBLE);
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).setVisibility(View.INVISIBLE);
            }

            if(repoItem.getKeyType().equals("3")){
                ((EditText) findViewById(R.id.repoSelPasswordText)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordText)).setVisibility(View.INVISIBLE);
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setText("");
                ((EditText) findViewById(R.id.repoSelPasswordPIN)).setVisibility(View.INVISIBLE);

                ((Button) findViewById(R.id.buttonSelectRepo)).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.keySelectRepoText)).setVisibility(View.INVISIBLE);
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).clearPattern();
                ((MaterialLockView) findViewById(R.id.patternSelectRepo)).setVisibility(View.VISIBLE);
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private void loadIntrusionRegister(){
        try{
            TableLayout ll = (TableLayout) findViewById(R.id.displayLinear);
            ll.removeAllViews();
            TableRow row = new TableRow(this);
            TextView data = new TextView(this);
            data.setText("      DATA      ");
            data.setGravity(Gravity.CENTER);
            data.setTypeface(null, Typeface.BOLD);
            TextView hora = new TextView(this);
            hora.setText("      HORA      ");
            hora.setGravity(Gravity.CENTER);
            hora.setTypeface(null, Typeface.BOLD);
            TextView clau = new TextView(this);
            clau.setText("      CLAU      ");
            clau.setTypeface(null, Typeface.BOLD);
            clau.setGravity(Gravity.CENTER);
            TextView foto = new TextView(this);
            foto.setText("      FOTO      ");
            foto.setTypeface(null, Typeface.BOLD);
            foto.setGravity(Gravity.CENTER);
            row.addView(data);
            row.addView(hora);
            row.addView(clau);
            row.addView(foto);
            row.setId(new Integer(-1));
            ll.addView(row);

            ArrayList<IntrusionRegisterItem> items = new ArrayList<IntrusionRegisterItem>();

            String dataPath = Environment.getExternalStorageDirectory().getPath().toString() + GeneralConstants.intrusionRegisterDataPath;
            File aux = new File(dataPath);
            if (!aux.exists()) {
                if (!aux.mkdirs()) {
                    Toast.makeText(context, "Error al crear el directori del registre d'intrusions", Toast.LENGTH_LONG).show();
                }
            }
            File[] image = aux.listFiles();
            IntrusionRegisterItem itemAux = null;
            for (int i = 0; i < image.length; i++) {
                try {
                    String index = image[i].getName().toString().substring(1, image[i].getName().toString().length()-4);
                    BufferedReader br = new BufferedReader(new FileReader(image[i]));
                    String result = "";
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        result += line;
                    }
                    String intrusionData = sm.decryptTextRSA(result, GeneralConstants.privateKey);
                    String[] intrusionDataArray = intrusionData.split(";");
                    String imagePath = Environment.getExternalStorageDirectory().getPath().toString() + GeneralConstants.intrusionRegisterImagesPath + "." + index + ".jpg";
                    itemAux = new IntrusionRegisterItem(Integer.parseInt(index), intrusionDataArray[0], intrusionDataArray[1], imagePath, intrusionDataArray[2], intrusionDataArray[3]);
                    items.add(itemAux);
                } catch (Throwable e) {
                    System.out.println(e);
                }
            }
            GeneralConstants.intrusionRegisterItems = items;
            for(IntrusionRegisterItem item: items){
                row = new TableRow(this);
                data = new TextView(this);
                data.setText(item.getData());
                data.setGravity(Gravity.CENTER);
                hora = new TextView(this);
                hora.setText(item.getHora());
                hora.setGravity(Gravity.CENTER);
                clau = new TextView(this);
                clau.setText(item.getClau());
                clau.setGravity(Gravity.CENTER);
                foto = new TextView(this);
                foto.setText("VEURE");
                foto.setGravity(Gravity.CENTER);
                row.addView(data);
                row.addView(hora);
                row.addView(clau);
                row.addView(foto);
                row.setId(item.getId());
                row.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        IntrusionRegisterItem item = GeneralConstants.intrusionRegisterItems.get(v.getId()-1);
                        if(item != null && item.getFotoPath() != null && item.getFotoPath().length()>5){
                            Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
                            intent.putExtra("path", item.getFotoPath());
                            intent.putExtra("symetricKey", item.getSymetricKey());
                            intent.putExtra("rsa", true);
                            //Start details activity
                            startActivity(intent);
                        }else{
                            Toast.makeText(context, "No existeix cap foto de l'intrús", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                ll.addView(row);
            }

        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    private static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }


/*
    private void writeToFile(String data){
        try{
            File out;
            OutputStreamWriter outStreamWriter = null;
            FileOutputStream outStream = null;
            String directory = Environment.getExternalStorageDirectory().getPath().toString() + "/CriptoFoto/";
            String filename = "logTest.txt";
            out = new File(new File(directory), filename);

            if ( out.exists() == false ){
                out.createNewFile();
            }

            outStream = new FileOutputStream(out, true) ;
            outStreamWriter = new OutputStreamWriter(outStream);

            outStreamWriter.append(data+"\n");
            outStreamWriter.flush();
        }catch(Throwable e){

        }
    }
*/
}
