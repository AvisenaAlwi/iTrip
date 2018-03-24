package com.internshipbcc.itrip;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Sena on 22/03/2018.
 */

public class FragmentAccount extends Fragment implements View.OnClickListener {

    ConstraintLayout clUnLoginLayout, clLoginLayout;
    LinearLayout llLogin, llRegis;

    EditText etEmailRegis, etPasswordRegis, etRePasswordRegis;
    EditText etEmailLogin, etPasswordLogin;

    Button btnRegister, btnLogin, btnLogout;

    CircleImageView imgProfile;
    TextView tvNama, tvEmail, tvWishlistCount, tvReservasiCount;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        mAuth = FirebaseAuth.getInstance();
        llLogin = rootView.findViewById(R.id.ll_login_container);
        llRegis = rootView.findViewById(R.id.ll_regis_container);

        imgProfile = rootView.findViewById(R.id.profile_image);
        tvNama = rootView.findViewById(R.id.tv_nama);
        tvEmail = rootView.findViewById(R.id.tv_email);
        tvWishlistCount = rootView.findViewById(R.id.tv_wishlist_count);
        tvReservasiCount = rootView.findViewById(R.id.tv_reservasi_count);

        etEmailRegis = rootView.findViewById(R.id.et_email_regis);
        etPasswordRegis = rootView.findViewById(R.id.et_password_regis);
        etRePasswordRegis = rootView.findViewById(R.id.et_repassword_regis);

        etEmailLogin = rootView.findViewById(R.id.et_email_login);
        etPasswordLogin = rootView.findViewById(R.id.et_password_login);

        btnRegister = rootView.findViewById(R.id.btn_regis);
        btnLogin = rootView.findViewById(R.id.btn_login);
        btnLogout = rootView.findViewById(R.id.btn_logout);

        clUnLoginLayout = rootView.findViewById(R.id.unlogin_layout);
        clLoginLayout = rootView.findViewById(R.id.logined_layout);

        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        (rootView.findViewById(R.id.tv_belum_punya_akun)).setOnClickListener(this);
        (rootView.findViewById(R.id.tv_sudah_punya_akun)).setOnClickListener(this);
        (rootView.findViewById(R.id.constraint_wishlist)).setOnClickListener(this);
        (rootView.findViewById(R.id.constraint_reservasi)).setOnClickListener(this);
        if (mAuth.getCurrentUser() != null)
            logined(true);
        return rootView;
    }

    public void logined(boolean logined) {
        if (logined) {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + user.getUid());
                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tvWishlistCount.setText(dataSnapshot.child("wishlist").getChildrenCount() + "");
                        tvReservasiCount.setText(dataSnapshot.child("reservasi").getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                reloadUiUser(user);
                user.reload().addOnSuccessListener(aVoid -> {
                    reloadUiUser(user);
                });
            }
            Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slideup);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    clUnLoginLayout.setVisibility(View.GONE);
                    clLoginLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            clLoginLayout.startAnimation(slideUp);
        } else {
            Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slideup);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    clLoginLayout.setVisibility(View.GONE);
                    clUnLoginLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            clUnLoginLayout.startAnimation(slideUp);
        }

    }

    private void reloadUiUser(FirebaseUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().equalsIgnoreCase(""))
            tvNama.setText(user.getDisplayName());
        else tvNama.setText(user.getEmail());
        tvEmail.setText(user.getEmail());
        if (user.getPhotoUrl() != null)
            Glide.with(getActivity())
                    .load(user.getPhotoUrl())
                    .thumbnail(0.6f)
                    .into(imgProfile);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_regis:
                registerProses();
                break;
            case R.id.btn_login:
                loginProses();
                break;
            case R.id.btn_logout:
                logoutProses();
                break;
            case R.id.tv_belum_punya_akun:
                moveToRegis();
                break;
            case R.id.tv_sudah_punya_akun:
                moveToLogin();
                break;
            case R.id.constraint_wishlist:
                Intent i = new Intent(getActivity(), WishlistActivity.class);
                i.putExtra("uid", mAuth.getCurrentUser().getUid());
                if (getActivity() != null)
                    getActivity().startActivity(i);
                break;
            case R.id.constraint_reservasi:
                Intent ii = new Intent(getActivity(), MyReservasiActivity.class);
                if (getActivity() != null)
                    getActivity().startActivity(ii);
                break;
        }
    }

    private void loginProses() {
        if (etEmailLogin.getText().toString().equalsIgnoreCase("")) {
            etEmailLogin.setError("Email tidak boleh kosong");
            return;
        } else if (etPasswordLogin.getText().toString().equalsIgnoreCase("")) {
            etPasswordLogin.setError("Password tidak boleh kosong");
            return;
        }
        ProgressDialog pg = new ProgressDialog(getActivity());
        pg.setIndeterminate(true);
        pg.show();
        String email = etEmailLogin.getText().toString();
        String password = etPasswordLogin.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            pg.dismiss();
            Toast.makeText(getActivity(), "Login Sukses", Toast.LENGTH_LONG).show();
            etEmailLogin.setText("");
            etPasswordLogin.setText("");
            logined(true);
        }).addOnFailureListener(authError -> {
            pg.dismiss();
            Toast.makeText(getActivity(), "Login gagal", Toast.LENGTH_LONG).show();
        });

        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void registerProses() {
//        if(etPasswordRegis.getText().toString()!=etRePasswordRegis.getText().toString()) {
//            Toast.makeText(getActivity(), "Password tidak sama", Toast.LENGTH_LONG).show();
//            return;
//        }
        ProgressDialog pg = new ProgressDialog(getActivity());
        pg.setIndeterminate(true);
        pg.show();

        String email = etEmailRegis.getText().toString();
        String password = etPasswordRegis.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            //Sukses bikin akun
            pg.dismiss();
            Toast.makeText(getActivity(), "Akun berhasil dibuat. Silahkan login", Toast.LENGTH_LONG).show();
            etEmailRegis.setText("");
            etPasswordRegis.setText("");
            etRePasswordRegis.setText("");
            moveToLogin();
        }).addOnFailureListener(e -> {
            pg.dismiss();
            Toast.makeText(getActivity(), "Pastikan email Anda benar.", Toast.LENGTH_LONG).show();
        });
        if (getActivity() != null) {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void logoutProses() {
        new MaterialDialog.Builder(getActivity())
                .title("Yakin?")
                .content("Yakin ingin logout?")
                .positiveText("YA")
                .negativeText("TIDAK")
                .onPositive((dialog, which) -> {
                    dialog.dismiss();
                    logined(false);
                    FirebaseAuth.getInstance().signOut();
                })
                .onNegative((dialog, which) -> {
                    dialog.dismiss();
                }).build().show();
    }

    public void moveToRegis() {
        llLogin.setVisibility(View.GONE);
        Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slideup);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                llRegis.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llRegis.startAnimation(slideUp);
    }

    public void moveToLogin() {
        llRegis.setVisibility(View.GONE);
        Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slideup);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                llLogin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llLogin.startAnimation(slideUp);
    }
}
