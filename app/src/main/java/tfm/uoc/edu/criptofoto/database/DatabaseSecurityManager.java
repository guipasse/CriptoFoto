package tfm.uoc.edu.criptofoto.database;

import android.os.Environment;
import android.util.Base64;
import tfm.uoc.edu.criptofoto.model.RepoItem;
import tfm.uoc.edu.criptofoto.constants.MainConstants.GeneralConstants;
import tfm.uoc.edu.criptofoto.constants.MainConstants.SQLConstants;
import tfm.uoc.edu.criptofoto.security.SecurityManager;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Implementació amb els mètodes del sistema de seguretat de la base de dades
 */
public class DatabaseSecurityManager implements DatabaseSecurityAPI {

    private static SQLiteDatabase database = null;
    private static SecurityManager sm = null;

    @Override
    public Boolean checkDatabaseFileExists(File databaseFile) throws Throwable{
        try{
            return databaseFile.exists();
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    @Override
    public Boolean autenticateUser(File databaseFile, String key) throws Throwable{
        Cursor cursor = null;
        Integer count = 0;
        try{
            sm = new SecurityManager();
            GeneralConstants.databasePassword = sm.secretKeyToString(sm.generateDatabaseKey(key.toCharArray()));
            initializeSQLCipher(databaseFile);
            Cursor cursorCount = database.rawQuery(SQLConstants.select_REPOSITORI_DEFECTE_COUNT, null);
            if (cursorCount.moveToFirst()){
                do{
                    count = cursorCount.getInt(0);
                } while (cursorCount.moveToNext());
            }
            if(count==0){
                cursor = database.rawQuery(SQLConstants.select_USER_DATA_LOGIN_FROM_KEY + GeneralConstants.databasePassword + "'", null);
            }else{
                cursor = database.rawQuery(SQLConstants.select_USER_DATA_LOGIN_FROM_KEY_DEFECTE + GeneralConstants.databasePassword + "'", null);
            }
            if (cursor.moveToFirst()){
                do{
                    String userName = cursor.getString(0);
                    String repoName = cursor.getString(1);
                    String repoCryptoKey = cursor.getString(2);
                    String repoIV = cursor.getString(3);
                    String repoKeyType = cursor.getString(4);
                    String repoId = cursor.getString(5);
                    String repoKey = cursor.getString(6);
                    String repoPath = cursor.getString(7);
                    String def = cursor.getString(8);
                    String privateKey = cursor.getString(9);

                    GeneralConstants.currentUserName = userName;
                    GeneralConstants.selectedRepositoriName = repoName;
                    GeneralConstants.selectedRepositoriCryptoKey = repoCryptoKey;
                    GeneralConstants.selectedRepositoriIV = repoIV;
                    GeneralConstants.selectedRepositoriTypeKey = new Integer(repoKeyType);
                    GeneralConstants.selectedRepositoriId = new Integer(repoId);
                    GeneralConstants.selectedRepositoriKey = repoKey;
                    GeneralConstants.selectedRepositoriPath = repoPath;
                    GeneralConstants.selectedRepositoriDefault = new Integer(def);
                    sm.setPrivateKey(privateKey);

                    break;
                } while (cursor.moveToNext());
            }else{
                return false;
            }
            return true;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    @Override
    public Boolean createUser(File databaseFile, String key, String keyType, String name) throws Throwable{
        Integer id = 1;
        try{
            sm = new SecurityManager();
            GeneralConstants.databasePassword = sm.secretKeyToString(sm.generateDatabaseKey(key.toCharArray()));
            initializeSQLCipher(databaseFile);
            String privateKey = sm.generatePublicPrivateKeys();
            database.beginTransaction();
            database.execSQL(SQLConstants.insert_USUARI,new Object[]{1, name, new Integer(keyType), GeneralConstants.databasePassword, privateKey});
            Cursor cursor = database.rawQuery(SQLConstants.select_MAX_PK_REPOSITORI, null);
            if (cursor.moveToFirst()){
                do{
                    if(cursor.getString(0)!=null){
                        id = Integer.parseInt(cursor.getString(0)) + 1;
                    }
                } while (cursor.moveToNext());
            }
            String repoCryptoKey = sm.generateAESKey();
            String repoIV = Base64.encodeToString(sm.generateBytesForIV(), Base64.DEFAULT).toString();
            database.execSQL(SQLConstants.insert_REPOSITORI,new Object[]{id, "PER DEFECTE", 1,  new Integer(keyType), key, 1,
                    repoCryptoKey, repoIV, "/CriptoFoto/.images/.default"});
            GeneralConstants.selectedRepositoriName = "PER DEFECTE";
            GeneralConstants.selectedRepositoriCryptoKey = repoCryptoKey;
            GeneralConstants.selectedRepositoriIV = repoIV;
            GeneralConstants.selectedRepositoriTypeKey = new Integer(keyType);
            GeneralConstants.selectedRepositoriId = id;
            GeneralConstants.selectedRepositoriKey = key;
            GeneralConstants.selectedRepositoriPath = "/CriptoFoto/.images/.default";
            GeneralConstants.selectedRepositoriDefault = 1;
            database.setTransactionSuccessful();
            return true;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }

    @Override
    public void editUser(String userName) throws Throwable{
        try{
            database.beginTransaction();
            database.execSQL(SQLConstants.update_USUARI,new Object[]{userName});
            GeneralConstants.currentUserName = userName;
            database.setTransactionSuccessful();
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }


    @Override
    public Boolean createRepo(String key, String keyType, String name, Boolean defecte) throws Throwable{
        String repoNamePath = null;
        Integer def = 0;
        Integer id = 1;
        try{
            sm = new SecurityManager();
            database.beginTransaction();
            Cursor cursor = database.rawQuery(SQLConstants.select_MAX_PK_REPOSITORI, null);
            if (cursor.moveToFirst()){
                do{
                    if(cursor.getString(0)!=null){
                        id = Integer.parseInt(cursor.getString(0)) + 1;
                    }
                } while (cursor.moveToNext());
            }
            String repoCryptoKey = sm.generateAESKey();
            String repoIV = Base64.encodeToString(sm.generateBytesForIV(), Base64.DEFAULT).toString();
            if(defecte){
                def = 1;
                database.execSQL(SQLConstants.update_CLEAN_ALL_REPOSITORI_DEFECTE);
            }
            repoNamePath = getRepoNameForPath();
            database.execSQL(SQLConstants.insert_REPOSITORI,new Object[]{id, name, 1,  new Integer(keyType), key, def,
                    repoCryptoKey, repoIV, "/CriptoFoto/.images/." + repoNamePath});
            GeneralConstants.selectedRepositoriName = name;
            GeneralConstants.selectedRepositoriCryptoKey = repoCryptoKey;
            GeneralConstants.selectedRepositoriIV = repoIV;
            GeneralConstants.selectedRepositoriTypeKey = new Integer(keyType);
            GeneralConstants.selectedRepositoriId = id;
            GeneralConstants.selectedRepositoriKey = key;
            GeneralConstants.selectedRepositoriPath = "/CriptoFoto/.images/." + repoNamePath;
            GeneralConstants.selectedRepositoriDefault = def;
            database.setTransactionSuccessful();
            return true;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }

    @Override
    public Boolean editRepo(String key, String keyType, String name, Boolean defecte) throws Throwable{
        Integer def = 0;
        try{
            database.beginTransaction();
            if(defecte){
                def = 1;
                database.execSQL(SQLConstants.update_CLEAN_ALL_REPOSITORI_DEFECTE);
            }
            database.execSQL(SQLConstants.update_REPOSITORI, new Object[]{name, new Integer(keyType), key, def, GeneralConstants.selectedRepositoriId});
            GeneralConstants.selectedRepositoriName = name;
            GeneralConstants.selectedRepositoriTypeKey = new Integer(keyType);
            GeneralConstants.selectedRepositoriKey = key;
            GeneralConstants.selectedRepositoriDefault = def;
            database.setTransactionSuccessful();
            return true;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }

    @Override
    public ArrayList<RepoItem> getRepositoriesForCombo() throws Throwable{
        ArrayList<RepoItem> repos = null;
        RepoItem repoItem = null;
        try{
            repos = new ArrayList<RepoItem>();
            database.beginTransaction();
            Cursor cursor = database.rawQuery(SQLConstants.select_ALL_REPOSITORI, null);
            if (cursor.moveToFirst()){
                do{
                    repoItem = new RepoItem(new Integer(cursor.getString(0)), cursor.getString(1),
                            cursor.getString(2), cursor.getString(3), cursor.getString(4),
                            cursor.getString(5),new Integer(cursor.getString(6)), cursor.getString(7));
                    repos.add(repoItem);
                } while (cursor.moveToNext());
            }
            database.setTransactionSuccessful();
            GeneralConstants.repositoriesForCombo = repos;
            return repos;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }

    private void initializeSQLCipher(File databaseFile) throws Throwable{
        try{
            if(!GeneralConstants.databaseInitializated){
                if(!databaseFile.exists()){
                    String path = Environment.getExternalStorageDirectory().getPath().toString() + "/CriptoFoto";
                    File gallery= new File(path);
                    if(gallery.exists()){
                        deleteRecursive(gallery);
                    }
                    databaseFile.mkdirs();
                    databaseFile.delete();
                    database = SQLiteDatabase.openOrCreateDatabase(databaseFile, GeneralConstants.databasePassword, null);
                    createDatabaseInitialModel();
                    loadDatabaseInitialData();
                }else{
                    database = SQLiteDatabase.openOrCreateDatabase(databaseFile, GeneralConstants.databasePassword, null);
                }
            }
            GeneralConstants.databaseInitializated = true;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }
    }

    private void createDatabaseInitialModel() throws Throwable{
        try{
            database.beginTransaction();
            database.execSQL(SQLConstants.createTable_TIPUS_CONTRASENYA);
            database.execSQL(SQLConstants.createTable_USUARI);
            database.execSQL(SQLConstants.createIndex_USUARI_1);
            database.execSQL(SQLConstants.createTable_REPOSITORI);
            database.execSQL(SQLConstants.createIndex_REPOSITORI_1);
            database.execSQL(SQLConstants.createIndex_REPOSITORI_2);
            database.setTransactionSuccessful();
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }

    private void loadDatabaseInitialData() throws Throwable{
        try{
            database.beginTransaction();
            database.execSQL(SQLConstants.insert_TIPUS_CONTRASENYA,new Object[]{1, "Text"});
            database.execSQL(SQLConstants.insert_TIPUS_CONTRASENYA,new Object[]{2, "PIN"});
            database.execSQL(SQLConstants.insert_TIPUS_CONTRASENYA,new Object[]{3, "Patró"});
            database.setTransactionSuccessful();
        }catch(Throwable e){
            System.out.println(e);
            throw e;
        }finally{
            database.endTransaction();
        }
    }

    private String getRepoNameForPath() throws Throwable{
        try{
            String chars = "abcdefghijklmnoprstuvwxyz";
            StringBuilder salt = new StringBuilder();
            Random rnd = new Random();
            while (salt.length() < 10) { // length of the random string.
                int index = (int) (rnd.nextFloat() * chars.length());
                salt.append(chars.charAt(index));
            }
            String saltStr = salt.toString();
            return saltStr;
        }catch(Throwable e){
            System.out.println(e);
            throw e;
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

}
