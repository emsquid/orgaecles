package fr.em.orgaecls.popups

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import fr.em.orgaecls.CampActivity
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.adapters.WidgetAdapter
import fr.em.orgaecls.adapters.WidgetItemDecoration
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.databases.CampChildrenRepository
import fr.em.orgaecls.databases.ChildrenRepository
import fr.em.orgaecls.models.ChildModel
import java.util.*

class ChildPopup(
    private val context: CampActivity,
    private val child: ChildModel?,
    private val campUid: String,
) : Dialog(context) {
    private lateinit var medicationAdapter: WidgetAdapter
    private lateinit var phobiaAdapter: WidgetAdapter
    private lateinit var dietAdapter: WidgetAdapter
    private lateinit var allergyAdapter: WidgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_child)
        setCanceledOnTouchOutside(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupComponents()
        setupButtons()
    }

    private fun setupComponents() {
        findViewById<EditText>(R.id.popup_child_firstname).setText(child?.firstName)
        findViewById<EditText>(R.id.popup_child_name).setText(child?.name)
        findViewById<EditText>(R.id.popup_child_age).setText(child?.age?.toString())

        val autonomyInput = findViewById<AutoCompleteTextView>(R.id.popup_child_autonomy)
        autonomyInput.setText(child?.autonomy)
        setupAutocompleteTextView(
            autonomyInput,
            context.resources.getStringArray(R.array.camp_page_child_autonomy_values).toList()
        )

        val enuresisInput = findViewById<AutoCompleteTextView>(R.id.popup_child_enuresis)
        enuresisInput.setText(child?.enuresis)
        setupAutocompleteTextView(
            enuresisInput,
            context.resources.getStringArray(R.array.camp_page_child_enuresis_values).toList()
        )

        val encopresisInput = findViewById<AutoCompleteTextView>(R.id.popup_child_encopresis)
        encopresisInput.setText(child?.encopresis)
        setupAutocompleteTextView(
            encopresisInput,
            context.resources.getStringArray(R.array.camp_page_child_encopresis_values).toList()
        )

        val moneyInput = findViewById<AutoCompleteTextView>(R.id.popup_child_money)
        moneyInput.setText(child?.money)
        setupAutocompleteTextView(
            moneyInput,
            context.resources.getStringArray(R.array.camp_page_child_money_values).toList()
        )

        val mailInput = findViewById<AutoCompleteTextView>(R.id.popup_child_mail)
        mailInput.setText(child?.mail)
        setupAutocompleteTextView(
            mailInput,
            context.resources.getStringArray(R.array.camp_page_child_mail_values).toList()
        )

        val medicationsInput = findViewById<EditText>(R.id.popup_child_medications)
        val medicationsHourInput =
            findViewById<AutoCompleteTextView>(R.id.popup_child_medications_hour)
        setupAutocompleteTextView(
            medicationsHourInput,
            context.resources.getStringArray(R.array.camp_page_child_medications_time).toList()
        )
        setupMedicationInput(medicationsInput, medicationsHourInput)

        val dietsInput = findViewById<EditText>(R.id.popup_child_diets)
        setupDietsInput(dietsInput)

        val allergiesInput = findViewById<EditText>(R.id.popup_child_allergies)
        setupAllergiesInput(allergiesInput)

        val phobiasInput = findViewById<EditText>(R.id.popup_child_phobias)
        setupPhobiasInput(phobiasInput)

        findViewById<EditText>(R.id.popup_child_notes).setText(child?.notes)
    }

    private fun setupAutocompleteTextView(view: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
        view.setAdapter(adapter)
    }

    private fun setupMedicationInput(medInput: EditText, medHourInput: AutoCompleteTextView) {
        val recyclerView = findViewById<RecyclerView>(R.id.popup_child_medications_recycler_view)
        val medications =
            child?.medications?.toList()?.map { "${it.first}-${it.second}" }?.toMutableList()
                ?: mutableListOf()
        this.medicationAdapter = WidgetAdapter(medications, true)
        recyclerView.adapter = this.medicationAdapter
        recyclerView.addItemDecoration(WidgetItemDecoration())

        val addButton = findViewById<ImageView>(R.id.popup_child_medications_add_button)
        addButton.setOnClickListener {
            val medication = medInput.text.toString()
            val medicationHour = medHourInput.text.toString()
            if (!TextUtils.isEmpty(medication) && !TextUtils.isEmpty(medicationHour)) {
                medicationAdapter.insertItem("$medication - $medicationHour")
            }
        }
    }

    private fun setupPhobiasInput(view: EditText) {
        val recyclerView = findViewById<RecyclerView>(R.id.popup_child_phobias_recycler_view)
        val phobias =
            child?.phobias?.keys?.toMutableList() ?: mutableListOf()
        this.phobiaAdapter = WidgetAdapter(phobias, true)
        recyclerView.adapter = this.phobiaAdapter
        recyclerView.addItemDecoration(WidgetItemDecoration())

        val addButton = findViewById<ImageView>(R.id.popup_child_phobias_add_button)
        addButton.setOnClickListener {
            val phobia = view.text.toString()
            if (!TextUtils.isEmpty(phobia)) phobiaAdapter.insertItem(phobia)
        }
    }

    private fun setupDietsInput(view: EditText) {
        val recyclerView = findViewById<RecyclerView>(R.id.popup_child_diets_recycler_view)
        val diets = child?.diets?.keys?.toMutableList() ?: mutableListOf()
        this.dietAdapter = WidgetAdapter(diets, true)
        recyclerView.adapter = this.dietAdapter
        recyclerView.addItemDecoration(WidgetItemDecoration())

        val addButton = findViewById<ImageView>(R.id.popup_child_diets_add_button)
        addButton.setOnClickListener {
            val diet = view.text.toString()
            if (!TextUtils.isEmpty(diet)) dietAdapter.insertItem(diet)
        }
    }

    private fun setupAllergiesInput(view: EditText) {
        val recyclerView = findViewById<RecyclerView>(R.id.popup_child_allergies_recycler_view)
        val allergies = child?.allergies?.keys?.toMutableList() ?: mutableListOf()
        this.allergyAdapter = WidgetAdapter(allergies, true)
        recyclerView.adapter = this.allergyAdapter
        recyclerView.addItemDecoration(WidgetItemDecoration())

        val addButton = findViewById<ImageView>(R.id.popup_child_allergies_add_button)
        addButton.setOnClickListener {
            val allergy = view.text.toString()
            if (!TextUtils.isEmpty(allergy)) allergyAdapter.insertItem(allergy)
        }
    }

    private fun setupButtons() {
        setupCloseButton()
        setupDeleteButton()
        setupSaveButton()
    }

    private fun setupCloseButton() {
        findViewById<ImageView>(R.id.popup_child_close_button).setOnClickListener {
            dismiss()
        }
    }

    private fun setupDeleteButton() {
        findViewById<ImageView>(R.id.popup_child_delete_button).setOnClickListener {
            if (child != null) {
                buildConfirmDialog(context) {
                    val childrenRepo = ChildrenRepository()
                    childrenRepo.deleteChild(child.uid, campUid)
                    dismiss()
                }
            } else {
                dismiss()
            }
        }
    }

    private fun setupSaveButton() {
        findViewById<ImageView>(R.id.popup_child_save_button).setOnClickListener {
            val repo = ChildrenRepository()

            val firstName =
                findViewById<EditText>(R.id.popup_child_firstname).text.toString()
            val name = findViewById<EditText>(R.id.popup_child_name).text.toString()
            val age = findViewById<EditText>(R.id.popup_child_age).text.toString()
            val autonomy =
                findViewById<EditText>(R.id.popup_child_autonomy).text.toString()
            val enuresis =
                findViewById<EditText>(R.id.popup_child_enuresis).text.toString()
            val encopresis =
                findViewById<EditText>(R.id.popup_child_encopresis).text.toString()
            val money = findViewById<EditText>(R.id.popup_child_money).text.toString()
            val mail = findViewById<EditText>(R.id.popup_child_mail).text.toString()
            val medications =
                this.medicationAdapter.itemList.map { it.replace('.', ',').split('-') }
                    .associate { it[0] to it[1] }
            val diets = this.dietAdapter.itemList.associateWith { true }
            val allergies = this.allergyAdapter.itemList.associateWith { true }
            val phobias = this.phobiaAdapter.itemList.associateWith { true }
            val notes = findViewById<EditText>(R.id.popup_child_notes).text.toString()

            if (firstName == "" || name == "" || age == "" || autonomy == "" || enuresis == "" || encopresis == "" || money == "" || mail == "") {
                Toast.makeText(
                    context,
                    "Un champ est un incomplet",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val newChild = ChildModel(
                    child?.uid ?: UUID.randomUUID().toString(),
                    campUid,
                    firstName,
                    name,
                    age.toInt(),
                    autonomy,
                    enuresis,
                    encopresis,
                    money,
                    mail,
                    medications,
                    diets,
                    allergies,
                    phobias,
                    notes
                )
                if (child != null) repo.updateChild(newChild) else repo.insertChild(newChild)
                dismiss()
            }
        }
    }
}