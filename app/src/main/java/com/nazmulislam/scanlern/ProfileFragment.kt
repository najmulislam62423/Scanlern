package com.nazmulislam.scanlern

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var etProfileName: EditText
    private lateinit var etProfileAge: EditText
    private lateinit var btnSaveProfile: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var base64Image: String? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto)
        etProfileName = view.findViewById(R.id.etProfileName)
        etProfileAge = view.findViewById(R.id.etProfileAge)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)

        // Circle shape এ ছবি clip করা
        ivProfilePhoto.clipToOutline = true
        ivProfilePhoto.outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }

        val btnBackProfile = view.findViewById<android.widget.ImageView>(R.id.btnBackProfile)
        btnBackProfile.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        loadProfile()

        ivProfilePhoto.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)

            // Resize to keep file size small
            bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
            ivProfilePhoto.setImageBitmap(bitmap)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etProfileName.setText(document.getString("name") ?: "")
                    etProfileAge.setText(document.getString("age") ?: "")

                    val photoBase64 = document.getString("photoBase64")
                    if (!photoBase64.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            ivProfilePhoto.setImageBitmap(bitmap)
                            base64Image = photoBase64
                        } catch (e: Exception) {
                            // ignore, keep default
                        }
                    }
                }
            }
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        val name = etProfileName.text.toString().trim()
        val age = etProfileAge.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val profileData = hashMapOf<String, Any>(
            "name" to name,
            "age" to age
        )
        base64Image?.let { profileData["photoBase64"] = it }

        firestore.collection("users").document(userId)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}