package com.onlinemusic.wemu.fragment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.onlinemusic.wemu.*
import com.onlinemusic.wemu.databinding.FragmentProfileBinding
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities

class ProfileFragment : Fragment() {

    var sessionManager: SessionManager? = null

    lateinit var fragmentProfileBinding: FragmentProfileBinding

    lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentProfileBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false);
        val root = fragmentProfileBinding.root
        mainActivity=activity as MainActivity

        mainActivity.mBottomNavigationView?.visibility = View.VISIBLE

        sessionManager = SessionManager(requireContext())
        fragmentProfileBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        fragmentProfileBinding.tvuserName.text = sessionManager?.getUsername()

        if (sessionManager?.getTheme().equals("night")){

            fragmentProfileBinding.btnswitchOnOff.isChecked = false
            fragmentProfileBinding.btnswitchOnOff.text = "OFF"
           // fragmentProfileBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black,resources.newTheme()))
            fragmentProfileBinding.btnswitchOnOff.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))

            fragmentProfileBinding.tvTitle.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.tvuserName.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.tvLogout.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.tvSetting.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.tvTheme.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.tvSubscription.setTextColor(getResources().getColor(R.color.white,resources.newTheme()))
            fragmentProfileBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)


        }
        else
        {

            fragmentProfileBinding.btnswitchOnOff.isChecked = true
            fragmentProfileBinding.btnswitchOnOff.text = "ON"
           // fragmentProfileBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentProfileBinding.btnswitchOnOff.setTextColor(getResources().getColor(R.color.black))

            fragmentProfileBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.tvuserName.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.tvLogout.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.tvSetting.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.tvTheme.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.tvSubscription.setTextColor(getResources().getColor(R.color.black))
            fragmentProfileBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)



        }
        fragmentProfileBinding.btnProfdetails.setOnClickListener {
             val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_profiledetails)
          /*  val intent = Intent(requireActivity(), Profiledetails::class.java)
            startActivity(intent)*/

        }
        fragmentProfileBinding.subscriptionLl.setOnClickListener {
            val intent = Intent(requireActivity(), Payment::class.java)
            startActivity(intent)
        }

        fragmentProfileBinding.btnLogout.setOnClickListener {

           /* val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you really want to logout?")
            builder.setPositiveButton(
                "yes"
            ) { dialog, which ->
                sessionManager?.logoutUser()
                val intent = Intent(requireActivity(), Login::class.java)
                startActivity(intent)

            }
            builder.setNegativeButton(
                "No"
            ) { dialog, which -> dialog.cancel() }

            val alert = builder.create()
            alert.show()
*/

            Utilities.alertDialogUtil(mainActivity,"Logout !!","Do you really want to logout?",
                isCancelable = false,
                isPositive = true,
                isNegetive = true,
                isNeutral = false,"Yes","No","",object : Utilities.OnItemClickListener{
                    override fun onItemClickAction(
                        type: Int,
                        dialogInterface: DialogInterface
                    ) {
                        if (type==1){
                            sessionManager?.logoutUser()
                            val intent = Intent(mainActivity, Login::class.java)
                            startActivity(intent)
                            MainActivity.onLogout()
                            dialogInterface.dismiss()
                            mainActivity.finish()
                        }
                    }

                })

        }


        fragmentProfileBinding.btnProfileSetting?.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_settings)
        /*    val intent = Intent(requireActivity(), Setting::class.java)
            startActivity(intent)*/
        }

        fragmentProfileBinding.btnswitchOnOff.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sessionManager?.setTheme("day")
                mainActivity.changeTheme()
                fragmentProfileBinding.btnswitchOnOff.text = "ON"
                fragmentProfileBinding.btnswitchOnOff.setTextColor(getResources().getColor(R.color.black))
               // fragmentProfileBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))

                fragmentProfileBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.tvuserName.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.tvLogout.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.tvSetting.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.tvTheme.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.tvSubscription.setTextColor(getResources().getColor(R.color.black))
                fragmentProfileBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)


            } else {
                sessionManager?.setTheme("night")
                mainActivity.changeTheme()
                fragmentProfileBinding.btnswitchOnOff.text = "OFF"
                fragmentProfileBinding.btnswitchOnOff.setTextColor(getResources().getColor(R.color.white))
               // fragmentProfileBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))


                fragmentProfileBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.tvuserName.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.tvLogout.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.tvSetting.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.tvTheme.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.tvSubscription.setTextColor(getResources().getColor(R.color.white))
                fragmentProfileBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)




            }
        })


        return root
    }

}