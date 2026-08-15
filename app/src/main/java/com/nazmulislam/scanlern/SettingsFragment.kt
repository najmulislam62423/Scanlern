package com.nazmulislam.scanlern

import android.content.Intent
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import java.io.File

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var auth: FirebaseAuth

    // ⚠️ এখানে তোমার নিজের সাপোর্ট email দিয়ে দাও
    private val supportEmail = "najmulislam6242@gmail.com"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val tvSettingsEmail = view.findViewById<TextView>(R.id.tvSettingsEmail)
        val btnChangePassword = view.findViewById<View>(R.id.btnChangePassword)
        val btnDeleteAccount = view.findViewById<View>(R.id.btnDeleteAccount)
        val btnLogoutSettings = view.findViewById<View>(R.id.btnLogoutSettings)
        val btnShareApp = view.findViewById<View>(R.id.btnShareApp)
        val btnSendFeedback = view.findViewById<View>(R.id.btnSendFeedback)
        val btnAboutApp = view.findViewById<View>(R.id.btnAboutApp)

        tvSettingsEmail.text = auth.currentUser?.email ?: "Unknown"
        val tvSettingsName = view.findViewById<TextView>(R.id.tvSettingsName)
        val ivSettingsPhoto = view.findViewById<ImageView>(R.id.ivSettingsPhoto)

        // প্রোফাইল ছবি গোল (circular) করার জন্য
        ivSettingsPhoto.clipToOutline = true
        ivSettingsPhoto.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setOval(0, 0, view.width, view.height)
            }
        }

        loadProfileInfo(tvSettingsName, ivSettingsPhoto)

        val profileCard = view.findViewById<View>(R.id.profileCard)
        profileCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        btnChangePassword.setOnClickListener {
            sendPasswordResetEmail()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        btnLogoutSettings.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        btnShareApp.setOnClickListener {
            shareApp()
        }

        btnSendFeedback.setOnClickListener {
            sendFeedback()
        }

        btnAboutApp.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email
        if (email == null) {
            Toast.makeText(requireContext(), "No email found", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This will permanently delete your account. This action cannot be undone. Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}. You may need to re-login before deleting.", Toast.LENGTH_LONG).show()
            }
    }

    private fun shareApp() {
        try {
            val context = requireContext()
            val sourceApk = File(context.applicationInfo.sourceDir)
            val apkCopy = File(context.cacheDir, "Scanlern.apk")

            // চলমান app এর APK নিজের cache folder এ copy করা (FileProvider দিয়ে share করার জন্য)
            sourceApk.copyTo(apkCopy, overwrite = true)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkCopy
            )

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/vnd.android.package-archive"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Check out Scanlern — an AI-powered study assistant! Install this APK to try it out. 📚✨"
            )
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(shareIntent, "Share Scanlern via"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to share app: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendFeedback() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Scanlern App Feedback")
            startActivity(Intent.createChooser(intent, "Send Feedback"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Scanlern")
            .setMessage(
                "Scanlern v1.0\n\n" +
                        "An AI-powered study assistant — scan your notes, get instant summaries, flashcards, and quizzes.\n\n" +
                        "Made with AI to help students study smarter. Made By (NAZMUL ISLAM)"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadProfileInfo(tvName: TextView, ivPhoto: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                if (!name.isNullOrEmpty()) {
                    tvName.text = name
                } else {
                    tvName.text = "Welcome!"
                }

                val photoBase64 = document.getString("photoBase64")
                if (!photoBase64.isNullOrEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ivPhoto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
    }
}