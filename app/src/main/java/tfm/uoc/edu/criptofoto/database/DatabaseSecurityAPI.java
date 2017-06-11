package tfm.uoc.edu.criptofoto.database;

import java.io.File;
import java.util.ArrayList;

import tfm.uoc.edu.criptofoto.model.RepoItem;

/**
 * Interfície amb els mètodes del sistema de seguretat de la base de dades
 */
public interface DatabaseSecurityAPI {

    /**
     * Mètode per comprovar si existeix el fitxer de la base de dades i per tant l'usuari
     * @throws Throwable
     */
    Boolean checkDatabaseFileExists(File databaseFile) throws Throwable;

    /**
     * Mètode per comprovar si existeix el fitxer de la base de dades i per tant l'usuari
     * @throws Throwable
     */
    public Boolean autenticateUser(File databaseFile, String key) throws Throwable;

    /**
     * Mètode per comprovar si existeix el fitxer de la base de dades i per tant l'usuari
     * @throws Throwable
     */
    public Boolean createUser(File databaseFile, String key, String keyType, String name) throws Throwable;

    /**
     *  Mètode per editar les propietats de l'usuari
     * @throws Throwable
     */
    public void editUser(String userName) throws Throwable;

    /**
     * Mètode per crear un nou repositori
     * @throws Throwable
     */
    public Boolean createRepo(String key, String keyType, String name, Boolean defecte) throws Throwable;


    /**
     * Mètode per modificar les dades del repositori actual
     * @throws Throwable
     */
    public Boolean editRepo(String key, String keyType, String name, Boolean defecte) throws Throwable;

    /**
     * Mètode per obtindre les dades de tots els repositoris de la base de dades
     * @throws Throwable
     */
    public ArrayList<RepoItem> getRepositoriesForCombo() throws Throwable;

}
