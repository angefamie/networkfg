package ci.miage.mob.networkfg

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import ci.miage.mob.networkfg.Appartement
import ci.miage.mob.networkfg.Connexion
import ci.miage.mob.networkfg.Graphs
import ci.miage.mob.networkfg.Noeuds
import com.google.android.material.navigation.NavigationView
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var appartement: Appartement // Déclarer une variable pour votre vue personnalisée

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Configurer la barre d'outils
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configuration de l'écouteur de navigation
        setupNavigation()

        // Initialisation de l'appartement
        appartement = findViewById(R.id.appartement)

        // Ajouter une marge pour s'adapter aux barres de système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Configuration de l'écouteur de navigation
    private fun setupNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_reset_network -> {
                    handleResetNetwork()
                }
                R.id.menu_save_network -> {
                    handleSaveNetwork()
                }
                R.id.menu_display_saved_network -> {
                    handleDisplaySavedNetwork()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Gestion de la réinitialisation du réseau
    private fun handleResetNetwork() {
        appartement.clearContent()
        showToast("Réseau réinitialisé")
    }

    // Gestion de la sauvegarde du réseau
    private fun handleSaveNetwork() {
        showToast("Réseau sauvegardé")
        showSaveDialog()
    }

    // Gestion de l'affichage des réseaux sauvegardés
    private fun handleDisplaySavedNetwork() {
        showToast("Liste des réseaux sauvegardés")
        showSavedNetworksMenu()
    }

    // Afficher la boîte de dialogue de sauvegarde
    private fun showSaveDialog() {
        val dialogView = layoutInflater.inflate(R.layout.layout_save_dialog, null)
        val editTextFileName = dialogView.findViewById<EditText>(R.id.editTextFileName)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        buttonSave.setOnClickListener {
            val fileName = editTextFileName.text.toString()
            saveGraphToFile(appartement.graph, fileName)
            Toast.makeText(this, "Fichier sauvegardé sous le nom : $fileName", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // Enregistrer le graphique dans un fichier
    private fun saveGraphToFile(graphs: MutableList<Graphs>, fileName: String) {
        val gson = Gson()
        val json = gson.toJson(graphs)

        val file = File(this.filesDir, "$fileName.json")
        file.writeText(json)
    }

    // Afficher le menu des réseaux sauvegardés
    private fun showSavedNetworksMenu() {
        val savedNetworkFiles = getListOfSavedFiles()

        if (savedNetworkFiles.isEmpty()) {
            showToast("Aucun réseau sauvegardé trouvé")
            return
        }

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Réseaux sauvegardés")

        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, savedNetworkFiles)
        dialogBuilder.setAdapter(arrayAdapter) { _, position ->
            val selectedFileName = savedNetworkFiles[position]
            val selectedGraph = loadGraphFromFile(selectedFileName)
            displayGraph(selectedGraph)
        }

        dialogBuilder.setNegativeButton("Fermer", null)

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    // Obtenir la liste des fichiers sauvegardés
    private fun getListOfSavedFiles(): List<String> {
        val filesDir = this.filesDir
        return filesDir.list { _, name -> name.endsWith(".json") }.toList()
    }

    // Charger le graphique à partir du fichier
    private fun loadGraphFromFile(fileName: String): MutableList<Graphs> {
        val file = File(this.filesDir, fileName)
        val json = file.readText()
        val gson = Gson()
        return gson.fromJson(json, object : TypeToken<MutableList<Graphs>>() {}.type)
    }

    // Afficher le graphique
    private fun displayGraph(graph: MutableList<Graphs>) {
        appartement.setGraphdt(graph)
    }

    // Afficher un message toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Gestionnaire de clics sur les éléments du menu de l'ActionBar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu, menu)
        return true
    }

    // Gestionnaire de clics sur les éléments du menu de l'ActionBar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ajoutObjet -> {
                handleAddObject()
                true
            }
            R.id.ajoutConnexion -> {
                handleAddConnection()
                true
            }
            R.id.mmodifObjetEtConexion -> {
                handleModifyObjectOrConnection()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Gestion de l'ajout d'objet
    private fun handleAddObject() {
        showToast("Objet ajouté")
        val randomX = (Math.random() * appartement.width).toFloat()
        val randomY = (Math.random() * appartement.height).toFloat()
        val newNode = Noeuds(
            idNoeud = appartement.noeuds.size + 1,
            point = Pair(randomX.toDouble(), randomY.toDouble()),
            etiquette = "Node ${appartement.noeuds.size + 1}",
            couleur = appartement.colors.first { it.first == "Noir" }.second,
            epaisseur = 20
        )

        appartement.addNoeud(newNode)
        appartement.toggleAddNodeMode()
    }

    // Gestion de l'ajout de connexion
    private fun handleAddConnection() {
        showToast("Connexion possible")
        appartement.toggleAddConnectionMode()
    }

    // Gestion de la modification d'objet ou de connexion
    private fun handleModifyObjectOrConnection() {
        showToast("Modifier objet ou connexion")
        appartement.setModifyMode()
    }
}