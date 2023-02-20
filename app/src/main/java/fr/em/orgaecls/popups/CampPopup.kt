package fr.em.orgaecls.popups

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import fr.em.orgaecls.MainActivity
import fr.em.orgaecls.R
import fr.em.orgaecls.adapters.WidgetAdapter
import fr.em.orgaecls.adapters.WidgetItemDecoration
import fr.em.orgaecls.builders.buildConfirmDialog
import fr.em.orgaecls.databases.CampsRepository
import fr.em.orgaecls.databases.UsersRepository
import fr.em.orgaecls.models.CampModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class CampPopup(
    private val context: MainActivity,
    private val camp: CampModel?
) : Dialog(context) {
    private lateinit var emailAdapter: WidgetAdapter
    private val updatable = camp == null || camp.creatorEmail == context.user.email
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.popup_camp)
        setCanceledOnTouchOutside(false)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupComponents()
        setupButtons()
    }

    private fun setupComponents() {
        findViewById<EditText>(R.id.popup_camp_name).setText(camp?.name)

        val startInput = findViewById<EditText>(R.id.popup_camp_start)
        startInput.setText(camp?.start)
        setupDatePicker(startInput)

        val endInput = findViewById<EditText>(R.id.popup_camp_end)
        endInput.setText(camp?.end)
        setupDatePicker(endInput)

        val emailInput = findViewById<EditText>(R.id.popup_camp_animators)
        setupAnimatorInput(emailInput)
    }

    private fun setupDatePicker(view: EditText) {
        fun addDatePicker(view: EditText) {
            val materialDatePickerBuilder =
                MaterialDatePicker.Builder.datePicker()
            val datePicker = materialDatePickerBuilder.build()
            datePicker.addOnPositiveButtonClickListener {
                val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                view.setText(dateFormatter.format(date))

            }
            datePicker.show(context.supportFragmentManager, datePicker.toString())

            view.clearFocus()
        }

        view.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                addDatePicker(view)
            }
        }

        view.setOnClickListener {
            addDatePicker(view)
        }
    }

    private fun setupAnimatorInput(emailInput: EditText) {
        val recyclerView = findViewById<RecyclerView>(R.id.popup_camp_animators_recycler_view)
        val animatorsEmail =
            if (camp != null) camp.animators.keys.map {
                it.replace(',', '.')
            } as MutableList<String> else mutableListOf()

        this.emailAdapter = WidgetAdapter(animatorsEmail, updatable)
        recyclerView?.adapter = emailAdapter
        recyclerView?.addItemDecoration(WidgetItemDecoration())

        val addButton = findViewById<ImageView>(R.id.popup_camp_animators_add_button)
        addButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches()
            ) {
                emailAdapter.insertItem(email)
                // fetch userName
                UsersRepository().fetchUserName(email)
            }
        }
    }

    private fun setupButtons() {
        setupCloseButton()
        setupDeleteButton()
        setupSaveButton()
        if (!updatable) {
            findViewById<View>(R.id.popup_camp_separator).visibility = View.GONE
        }
    }

    private fun setupCloseButton() {
        findViewById<ImageView>(R.id.popup_camp_close_button).setOnClickListener {
            dismiss()
        }
    }

    private fun setupDeleteButton() {
        val deleteButton = findViewById<ImageView>(R.id.popup_camp_delete_button)
        if (!updatable) {
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.setOnClickListener {
                if (camp != null && camp.creatorEmail == context.user.email) {
                    buildConfirmDialog(context) {
                        val repo = CampsRepository()
                        repo.deleteCamp(camp)
                        dismiss()
                    }
                } else {
                    toast("Vous ne pouvez pas faire ça")
                    dismiss()
                }
            }
        }
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<ImageView>(R.id.popup_camp_save_button)
        if (!updatable) {
            saveButton.visibility = View.GONE
        } else {
            saveButton.setOnClickListener {
                val repo = CampsRepository()

                val name = findViewById<EditText>(R.id.popup_camp_name).text.toString()
                val start = findViewById<EditText>(R.id.popup_camp_start).text.toString()
                val end = findViewById<EditText>(R.id.popup_camp_end).text.toString()
                val animators = getAnimators()
                val userEmail = context.user.email!!

                if (name == "" || start == "" || end == "") {
                    toast("Un champ est incomplet")
                } else if (!checkDatesValidity(start, end)) {
                    toast("Les dates sont mal choisies")
                } else if (camp != null) {
                    if (camp.creatorEmail == userEmail) {
                        val updatedCamp = CampModel(
                            camp.uid,
                            name,
                            start,
                            end,
                            camp.creatorEmail,
                            animators
                        )
                        repo.updateCamp(camp, updatedCamp)
                    } else {
                        toast("Vous ne pouvez pas faire ça")
                    }
                    dismiss()
                } else {
                    val newCamp = CampModel(
                        UUID.randomUUID().toString(),
                        name,
                        start,
                        end,
                        userEmail,
                        animators
                    )
                    repo.insertCamp(newCamp)
                    dismiss()
                }
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkDatesValidity(start: String, end: String): Boolean {
        val firstDate = LocalDate.parse(start, dateFormatter)
        val secondDate = LocalDate.parse(end, dateFormatter)

        return firstDate.isBefore(secondDate)
    }

    private fun getAnimators(): Map<String, String> {
        val animators = (this.emailAdapter.itemList)
            .map { it.replace('.', ',') }
            .mapNotNull {
                val name = UsersRepository.Singleton.userNameMap[it]
                if (name != null) Pair(it, name) else null
            }
            .toMap()
            .toMutableMap()

        animators[context.user.email!!.replace('.', ',')] = context.user.displayName!!

        return animators
    }
}