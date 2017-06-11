package tfm.uoc.edu.criptofoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import tfm.uoc.edu.criptofoto.constants.MainConstants;
import tfm.uoc.edu.criptofoto.security.SecurityManager;

public class PhotoActivity extends AppCompatActivity {

    private SecurityManager sm;
    private ImageView imgDisplay;
    private ImageView imgDisplayLandscape;
    private Button btnClose;
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.content_photo);
            Bundle extras = getIntent().getExtras();
            String path= extras.getString("path");
            Boolean rsa = extras.getBoolean("rsa");
            image = new File(path);
            sm = new SecurityManager();
            Bitmap bmp = null;
            Bitmap bmpLandscape = null;
            btnClose = (Button) findViewById(R.id.btnClose);
            imgDisplay = (ImageView) findViewById(R.id.imgDisplay);
            imgDisplayLandscape = (ImageView) findViewById(R.id.imgDisplayLandscape);
            if(rsa){
                btnClose.setVisibility(View.INVISIBLE);
                String symetricKey = extras.getString("symetricKey");
                byte[] keyBytes = Base64.decode(symetricKey, Base64.DEFAULT);
                if(imgDisplay!=null){
                    bmp = sm.loadEncryptedImageFullScreen(MainConstants.GeneralConstants.ivBytesIntrusionImages, image, Base64.decode(symetricKey, Base64.DEFAULT), this);
                }
                if(imgDisplayLandscape!=null){
                    bmpLandscape = sm.loadEncryptedImageFullScreenLandscape(MainConstants.GeneralConstants.ivBytesIntrusionImages, image, Base64.decode(symetricKey, Base64.DEFAULT), this);
                }
            }else{
                btnClose.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        image.delete();
                        PhotoActivity.this.finish();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivityForResult(intent, MainConstants.GeneralConstants.requestImageDeleteAndClose);
                    }
                });
                if(imgDisplay!=null){
                    bmp = sm.loadEncryptedImageFullScreen(Base64.decode(MainConstants.GeneralConstants.selectedRepositoriIV, Base64.DEFAULT), image,
                            Base64.decode(MainConstants.GeneralConstants.selectedRepositoriCryptoKey, Base64.DEFAULT), this);
                }
                if(imgDisplayLandscape!=null){
                    bmpLandscape = sm.loadEncryptedImageFullScreenLandscape(Base64.decode(MainConstants.GeneralConstants.selectedRepositoriIV, Base64.DEFAULT), image,
                            Base64.decode(MainConstants.GeneralConstants.selectedRepositoriCryptoKey, Base64.DEFAULT), this);
                }
            }
            if(imgDisplay!=null){
                imgDisplay.setImageBitmap(bmp);
                imgDisplay.setScaleType(ImageView.ScaleType.FIT_XY);
            }
            if(imgDisplayLandscape!=null){
                imgDisplayLandscape.setImageBitmap(bmpLandscape);
                imgDisplayLandscape.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }catch(Throwable e){
            System.out.println(e);
        }
    }

}
