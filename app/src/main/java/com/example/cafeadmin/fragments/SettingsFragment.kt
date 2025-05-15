package com.example.cafeadmin.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cafeadmin.R
import com.example.cafeadmin.databinding.FragmentSettingsBinding
import com.example.cafeadmin.utils.LocaleManager
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var languageChanged = false

    private var initialSelection = true // birinchi marta spinner tanlanishini inkor etish uchun

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val languages = listOf(
            LanguageItem("O'zbekcha", R.drawable.ic_uzb),
            LanguageItem("Русский", R.drawable.ic_rus)
        )

        val adapter = object : BaseAdapter() {
            override fun getCount(): Int = languages.size
            override fun getItem(position: Int): LanguageItem = languages[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return createCustomView(position, convertView, parent, false)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return createCustomView(position, convertView, parent, true)
            }

            private fun createCustomView(position: Int, convertView: View?, parent: ViewGroup, isDropdown: Boolean): View {
                val view = convertView ?: LayoutInflater.from(parent.context).inflate(
                    if (isDropdown) R.layout.spinner_dropdown_item else R.layout.spinner_item,
                    parent, false
                )
                val textView = view.findViewById<TextView>(R.id.tv_language_name)
                val imageView = view.findViewById<ImageView>(R.id.iv_language_flag)
                val item = getItem(position)

                textView.text = item.name
                imageView.setImageResource(item.flagResId)
                return view
            }
        }

        binding.languageSpinner.adapter = adapter

        // Avvalgi tanlangan tilni tiklash
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("lang", "uz")
        binding.languageSpinner.setSelection(if (savedLang == "uz") 0 else 1)
        languageChanged = false

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (languageChanged) {
                    val languageCode = if (position == 0) "uz" else "ru"
                    prefs.edit().putString("lang", languageCode).apply()
                    LocaleManager.setNewLocale(requireContext(), languageCode)
                    requireActivity().recreate()
                }
                languageChanged = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun setLocale(languageCode: String) {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("lang", languageCode).apply()

        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class LanguageItem(val name: String, val flagResId: Int)
}
