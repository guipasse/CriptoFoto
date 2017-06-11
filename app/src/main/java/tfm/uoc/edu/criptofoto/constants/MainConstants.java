package tfm.uoc.edu.criptofoto.constants;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import tfm.uoc.edu.criptofoto.model.IntrusionRegisterItem;
import tfm.uoc.edu.criptofoto.model.RepoItem;

/**
 * Interfície de constants per a l'aplicació
 */
public interface MainConstants {

    /**
     * Classe de constants generals
     */
    public class GeneralConstants {

        public static final int cameraPermission = 1;
        public static final int writeExternalStoragePermission = 2;
        public static final int readExternalStoragePermission = 3;
        public static final int requestImageCapture = 11;
        public static final int requestImageDeleteAndClose = 21;

        public static String selectedRepositoriName = "";
        public static String selectedRepositoriIV = "";
        public static Integer selectedRepositoriId = 0;
        public static Integer selectedRepositoriTypeKey = 0;
        public static String selectedRepositoriCryptoKey = "";
        public static String selectedRepositoriKey = "";
        public static String selectedRepositoriPath = ""; // "/CriptoFoto/Images"  "/CriptoFoto/.images/.default"
        public static Integer selectedRepositoriDefault = 0;

        public static String currentUserName = "";

        public static Boolean userRegistered = false;
        public static Boolean userCreated = false;
        public static Boolean databaseInitializated = false;

        public static final String repositoryDirectory = "/CriptoFoto/.images/.";
        public static final String databaseName = "database.db";
        public static String databasePassword = "";

        public static final String salt = "x2M_";

        public static ArrayList<RepoItem> repositoriesForCombo = null;
        public static ArrayList<IntrusionRegisterItem> intrusionRegisterItems = null;
        public static Integer selectedRepositoriIdCombo = 0;

        public static final String intrusionRegisterImagesPath = "/CriptoFoto/.intrusionImages/";
        public static final String intrusionRegisterDataPath = "/CriptoFoto/.intrusionData/";
        public static final String publicKeyPath = "/CriptoFoto/.publicKey";
        public static PublicKey publicKey = null;
        public static PrivateKey privateKey = null;

        public static byte[] ivBytesIntrusionImages = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    }

    /**
     * Classe de constants SQL
     */
    public class SQLConstants {

        public static final String createTable_TIPUS_CONTRASENYA = "CREATE TABLE IF NOT EXISTS `TIPUS_CONTRASENYA` ( " +
                "  `TIPUS_CONTRASENYA_ID` INTEGER NOT NULL, " +
                "  `TIPUS_CONTRASENYA_DESC` TEXT NULL, " +
                "  PRIMARY KEY (`TIPUS_CONTRASENYA_ID`) " +
                "  )";

        public static final String createTable_USUARI = "CREATE TABLE IF NOT EXISTS `USUARI` ( " +
                "  `USUARI_ID` INTEGER NOT NULL, " +
                "  `USUARI_NOM` TEXT NOT NULL,\n" +
                "  `TIPUS_CONTRASENYA_ID` INTEGER NOT NULL, " +
                "  `CONTRASENYA` TEXT NOT NULL, " +
                "  `CONTRASENYA_PRIVADA` TEXT NOT NULL, " +
                "  PRIMARY KEY (`USUARI_ID`), " +
                "  FOREIGN KEY (`TIPUS_CONTRASENYA_ID`) REFERENCES `TIPUS_CONTRASENYA` (`TIPUS_CONTRASENYA_ID`) " +
                "  )";

        public static final String createIndex_USUARI_1 = "CREATE INDEX IF NOT EXISTS `fk_USUARI_TIPUS_CONTRASENYA_idx` ON " +
                "  `USUARI` (`TIPUS_CONTRASENYA_ID`);";

        public static final String createTable_REPOSITORI = "CREATE TABLE IF NOT EXISTS `REPOSITORI` ( " +
                "  `REPOSITORI_ID` INTEGER NOT NULL, " +
                "  `REPOSITORI_NOM` TEXT NOT NULL, " +
                "  `REPOSITORI_PATH` TEXT NOT NULL, " +
                "  `USUARI_ID` INTEGER NOT NULL, " +
                "  `TIPUS_CONTRASENYA_ID` INTEGER NOT NULL, " +
                "  `CONTRASENYA` TEXT NOT NULL, " +
                "  `DEFECTE` INTEGER NOT NULL DEFAULT 0, " +
                "  `CONTRASENYA_XIFRAT` TEXT NOT NULL, " +
                "  `IV_XIFRAT` TEXT NOT NULL, " +
                "  PRIMARY KEY (`REPOSITORI_ID`), " +
                "  FOREIGN KEY (`TIPUS_CONTRASENYA_ID`) REFERENCES `TIPUS_CONTRASENYA` (`TIPUS_CONTRASENYA_ID`) " +
                "  FOREIGN KEY (`USUARI_ID`) REFERENCES `USUARI` (`USUARI_ID`) " +
                "  )";

        public static final String createIndex_REPOSITORI_1 = "CREATE INDEX IF NOT EXISTS `fk_REPOSITORI_USUARI1_idx` " +
                "  ON `REPOSITORI` (`USUARI_ID`)";

        public static final String createIndex_REPOSITORI_2 = "CREATE INDEX IF NOT EXISTS `fk_REPOSITORI_TIPUS_CONTRASENYA1_idx` " +
                "  ON `REPOSITORI` (`TIPUS_CONTRASENYA_ID`)";

        public static final String insert_TIPUS_CONTRASENYA = "INSERT INTO TIPUS_CONTRASENYA (TIPUS_CONTRASENYA_ID, TIPUS_CONTRASENYA_DESC) VALUES (?, ?)";

        public static final String insert_USUARI = "INSERT INTO USUARI (USUARI_ID, USUARI_NOM, TIPUS_CONTRASENYA_ID, CONTRASENYA, CONTRASENYA_PRIVADA) VALUES (?, ?, ?, ?, ?)";

        public static final String insert_REPOSITORI = "INSERT INTO REPOSITORI (REPOSITORI_ID, REPOSITORI_NOM, USUARI_ID, " +
                " TIPUS_CONTRASENYA_ID, CONTRASENYA, DEFECTE, CONTRASENYA_XIFRAT, IV_XIFRAT, REPOSITORI_PATH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        public static final String select_REPOSITORI_DEFECTE_COUNT = "SELECT COUNT(*) FROM REPOSITORI WHERE DEFECTE = 1";

        public static final String select_USER_DATA_LOGIN_FROM_KEY_DEFECTE = "SELECT u.USUARI_NOM, r.REPOSITORI_NOM, r.CONTRASENYA_XIFRAT, " +
                " r.IV_XIFRAT, r.TIPUS_CONTRASENYA_ID, r.REPOSITORI_ID, r.CONTRASENYA, r.REPOSITORI_PATH, r.DEFECTE, u.CONTRASENYA_PRIVADA FROM" +
                " USUARI u " +
                " JOIN REPOSITORI r ON u.USUARI_ID = r.USUARI_ID " +
                " WHERE r.DEFECTE = 1 AND u.CONTRASENYA = '";

        public static final String select_USER_DATA_LOGIN_FROM_KEY = "SELECT u.USUARI_NOM, r.REPOSITORI_NOM, r.CONTRASENYA_XIFRAT, " +
                " r.IV_XIFRAT, r.TIPUS_CONTRASENYA_ID, r.REPOSITORI_ID, r.CONTRASENYA, r.REPOSITORI_PATH, r.DEFECTE, u.CONTRASENYA_PRIVADA FROM" +
                " USUARI u " +
                " JOIN REPOSITORI r ON u.USUARI_ID = r.USUARI_ID " +
                " WHERE u.CONTRASENYA = '";

        public static final String select_MAX_PK_REPOSITORI = "SELECT MAX(REPOSITORI_ID) FROM REPOSITORI";


        public static final String update_USUARI = "UPDATE USUARI SET USUARI_NOM = ? WHERE USUARI_ID = 1";

        public static final String update_CLEAN_ALL_REPOSITORI_DEFECTE = "UPDATE REPOSITORI SET DEFECTE = 0 ";

        public static final String update_REPOSITORI = "UPDATE REPOSITORI SET REPOSITORI_NOM = ?, TIPUS_CONTRASENYA_ID = ?, CONTRASENYA = ?, DEFECTE = ? " +
                " WHERE REPOSITORI_ID = ?";

        public static final String select_ALL_REPOSITORI = "SELECT REPOSITORI_ID, REPOSITORI_NOM, TIPUS_CONTRASENYA_ID, CONTRASENYA, " +
                " IV_XIFRAT, CONTRASENYA_XIFRAT, DEFECTE, REPOSITORI_PATH FROM REPOSITORI";

    }

}
