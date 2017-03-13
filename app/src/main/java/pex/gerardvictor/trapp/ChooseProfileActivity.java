package pex.gerardvictor.trapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ChooseProfileActivity extends AppCompatActivity {

    private Button professionalButton;
    private Button personalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_profile);

        professionalButton = (Button) findViewById(R.id.professional_button);
        personalButton = (Button) findViewById(R.id.personal_button);

        professionalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(ChooseProfileActivity.this, LoginActivity.class);
                startActivity(login);
            }
        });

        personalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(ChooseProfileActivity.this, LoginActivity.class);
                startActivity(login);
            }
        });
    }

}
