package com.iwinter.ppoint.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.iwinter.ppoint.interfaces.PaymentForm;
import com.iwinter.ppoint.R;
import com.iwinter.ppoint.interfaces.TokenList;
import com.iwinter.ppoint.dialog.ErrorDialogFragment;
import com.iwinter.ppoint.dialog.ProgressDialogFragment;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vladislav on 2016-03-12.
 */
public class PaymentFormActivity extends FragmentActivity {
    /*
     * Change this to your publishable key.
     *
     * You can get your key here: https://manage.stripe.com/account/apikeys
     */
    public static final String PUBLISHABLE_KEY = "pk_test_UVkZOLstLB0djNlAayBIOn6q";
    private static final String API_KEY = "sk_test_Rz1GhngPuKxIReNIG8JlfywB";

    private ProgressDialogFragment progressFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        progressFragment = ProgressDialogFragment.newInstance(R.string.progressMessage);
    }

    public void saveCreditCard(PaymentForm form) {
    try {
        Stripe stripe = new Stripe(PUBLISHABLE_KEY);
        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());
        card.setCurrency(form.getCurrency());
        if (card.validateCard()) {
            startProgress();
            stripe.createToken(card, new TokenCallback() {

                @Override
                public void onSuccess(Token token) {
                    finishProgress();
                    chargeCustomer(token);

                }

                @Override
                public void onError(Exception error) {
                    finishProgress();
                    Log.e("Error creating token.",
                            error.toString());
                }
            });
        }

        } catch (AuthenticationException e) {
        e.printStackTrace();
    }
    }

    public void chargeCustomer(Token token) {
        final Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", getIntent().getExtras().getInt("Amount"));
        chargeParams.put("currency", "usd");
        chargeParams.put("card", token.getId()); // obtained with Stripe.js

        new AsyncTask<Void, Void, Void>() {

            Charge charge;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    com.stripe.Stripe.apiKey = API_KEY;
                    charge = Charge.create(chargeParams);

                    Log.i("IsCharged", charge.getCreated().toString());


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result) {

            };

        }.execute();

    }

    private void startProgress() {
        progressFragment.show(getSupportFragmentManager(), "progress");
    }

    private void finishProgress() {
        progressFragment.dismiss();
    }

    private void handleError(String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validationErrors, error);
        fragment.show(getSupportFragmentManager(), "error");
    }

    private TokenList getTokenList() {
        return (TokenList)(getSupportFragmentManager().findFragmentById(R.id.token_list));
    }
}
