package com.example.ujicobaaplikasi.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.ui.admin.AdminActivity
import com.example.ujicobaaplikasi.ui.auth.LoginActivity
import com.example.ujicobaaplikasi.ui.common.AvatarView
import com.example.ujicobaaplikasi.ui.detail.NotificationsActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.example.ujicobaaplikasi.ui.settings.ChangePasswordActivity
import com.example.ujicobaaplikasi.ui.settings.EditProfileActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var avatar: AvatarView? = null
    private var nameView: TextView? = null
    private var emailView: TextView? = null
    private var metaView: TextView? = null
    private var bioView: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatar = view.findViewById(R.id.profileAvatar)
        nameView = view.findViewById(R.id.profileName)
        emailView = view.findViewById(R.id.profileEmail)
        metaView = view.findViewById(R.id.profileMeta)
        bioView = view.findViewById(R.id.profileBio)

        view.findViewById<View>(R.id.tombolEditProfilCepat).setOnClickListener { openEditProfile() }
        view.findViewById<View>(R.id.barisEditProfil).setOnClickListener { openEditProfile() }
        view.findViewById<View>(R.id.barisUbahPassword).setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }
        view.findViewById<View>(R.id.barisNotifikasiProfil).setOnClickListener {
            startActivity(
                Intent(requireContext(), NotificationsActivity::class.java)
                    .putExtra(
                        NotificationsActivity.EXTRA_NAV_DESTINATION,
                        BottomNavDestination.PROFILE.name,
                    ),
            )
        }
        view.findViewById<View>(R.id.barisBantuan).setOnClickListener { showHelp() }
        view.findViewById<View>(R.id.barisTentang).setOnClickListener { showAbout() }
        view.findViewById<View>(R.id.tombolLogoutProfil).setOnClickListener { confirmLogout() }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) refresh()
    }

    private fun refresh() {
        val account = AuthStore.currentAccount(requireContext()) ?: return
        val name = account.name.takeIf { it.isNotBlank() } ?: getString(R.string.profile_tagline)
        avatar?.setProfile(name, account.avatarUri)
        nameView?.text = name
        emailView?.text = account.email
        metaView?.text = if (account.city.isBlank()) {
            getString(R.string.profile_tagline)
        } else {
            "${account.city} · ${getString(R.string.profile_tagline)}"
        }
        bioView?.apply {
            text = account.bio
            visibility = if (account.bio.isBlank()) View.GONE else View.VISIBLE
        }
        if (account.isAdmin) addAdminPanelButton()
    }

    private fun addAdminPanelButton() {
        val logout = view?.findViewById<View>(R.id.tombolLogoutProfil) ?: return
        val parent = logout.parent as? LinearLayout ?: return
        if (parent.findViewWithTag<View>("admin_panel_button") != null) return
        val button = MaterialButton(requireContext()).apply {
            tag = "admin_panel_button"
            text = getString(R.string.admin_panel_title)
            setIconResource(R.drawable.ic_arrow_forward)
            setOnClickListener {
                startActivity(Intent(requireContext(), AdminActivity::class.java))
            }
        }
        val margin = (12 * resources.displayMetrics.density).toInt()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(margin, margin, margin, 0)
        }
        parent.addView(button, parent.indexOfChild(logout), params)
    }

    private fun openEditProfile() {
        startActivity(Intent(requireContext(), EditProfileActivity::class.java))
    }

    private fun showHelp() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.help_title)
            .setMessage(R.string.help_message)
            .setNegativeButton(R.string.action_close, null)
            .setPositiveButton(R.string.action_send_email) { _, _ -> sendSupportEmail() }
            .show()
    }

    private fun sendSupportEmail() {
        try {
            startActivity(
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:halo@jelahlombok.id")
                },
            )
        } catch (_: ActivityNotFoundException) {
            AppNotice.info(requireActivity(), R.string.help_no_email)
        }
    }

    private fun showAbout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.action_close, null)
            .show()
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.logout_confirm) { _, _ ->
                AuthStore.logout(requireContext())
                AppNotice.deferSuccess(requireActivity(), R.string.logout_success)
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                )
                requireActivity().finish()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        avatar = null
        nameView = null
        emailView = null
        metaView = null
        bioView = null
    }
}
