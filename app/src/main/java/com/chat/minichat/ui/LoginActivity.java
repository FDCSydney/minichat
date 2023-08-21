package com.chat.minichat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chat.minichat.databinding.ActivityLoginBinding;
import com.chat.minichat.repository.MainRepository;
import com.chat.minichat.utils.Constants;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityLoginBinding mBinding;
    private MainRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = mBinding.getRoot();
        setContentView(view);

        mRepository = MainRepository.getInstance(this);
        mBinding.btnLogin.setOnClickListener(this);
    }

    /**
     * @param view
     */
    @Override
    public void onClick(View view) {
        String username = Objects.requireNonNull(mBinding.usernameLayout.getEditText())
                .getText().toString().trim();
        String password = Objects.requireNonNull(mBinding.passwordLayout.getEditText())
                .getText().toString().trim();

        if (!validateUserName(username) | !validatePassword(password)) return;
        this.mRepository.login(username, password, (isSuccess, error) -> {
            if (!isSuccess) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });
    }

    private Boolean validateUserName(String username) {
        if (username.isEmpty()) {
            mBinding.passwordLayout.setError(Constants.Error.USER_REQUIRED);
            return false;
        }
        return true;
    }

    private Boolean validatePassword(String pass) {
        if (pass.isEmpty()) {
            mBinding.passwordLayout.setError(Constants.Error.PASS_REQUIRED);
            return false;
        }
        return true;
    }

}