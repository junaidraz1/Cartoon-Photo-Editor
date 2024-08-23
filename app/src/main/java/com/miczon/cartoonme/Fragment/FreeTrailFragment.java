package com.miczon.cartoonme.Fragment;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.miczon.cartoonme.Activities.ApplyEffectActivity;
import com.miczon.cartoonme.Activities.HomeActivity;
import com.miczon.cartoonme.Activities.SavedFileActivity;
import com.miczon.cartoonme.Activities.SplashActivity;
import com.miczon.cartoonme.Helper.InAppBillingHelper;
import com.miczon.cartoonme.Listeners.FragmentClickListener;
import com.miczon.cartoonme.Listeners.PremiumStatusChangeListener;
import com.miczon.cartoonme.Manager.ConnectionManager;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Constants;
import com.miczon.cartoonme.Utils.Utility;

import java.util.ArrayList;

public class FreeTrailFragment extends Fragment implements PremiumStatusChangeListener {

    public String TAG = "FreeTrailFragment", from = "", via = "";
    RelativeLayout layoutClose;
    LinearLayout layoutMonthlySub;
    TextView tvSubPrice, tvManageSub, tvPrivacyPolicy;
    Button buySubBtn;
    boolean isRefresh = false;
    boolean isBought;
    PrefsManager prefsManager;
    FragmentClickListener fragmentClickListener;
    ImageView ivTile1, ivTile2, ivTile3;

    ArrayList<String> filterIds;

    Bundle bundle;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SplashActivity) {
            fragmentClickListener = (FragmentClickListener) context;
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_free_trail, container, false);


        layoutClose = view.findViewById(R.id.rl_close);
        layoutMonthlySub = view.findViewById(R.id.ll_monthlySub);
        buySubBtn = view.findViewById(R.id.btn_freeTrail);
        tvSubPrice = view.findViewById(R.id.tv_subPrice);
        tvManageSub = view.findViewById(R.id.tv_cancelSub);
        tvPrivacyPolicy = view.findViewById(R.id.tv_privacyPolicy);
        ivTile1 = view.findViewById(R.id.iv1);
        ivTile2 = view.findViewById(R.id.iv2);
        ivTile3 = view.findViewById(R.id.iv3);

        InAppBillingHelper.getInstance().setPremiumStatusChangeListener(this);

        filterIds = new ArrayList<>();

        prefsManager = new PrefsManager(requireActivity());

        Utility.getInstance().addSplashAnimation(ivTile1, ivTile2, ivTile3, true);

        bundle = getArguments();
        if (bundle != null) {
            from = bundle.getString("from");
            via = bundle.getString("via");
            filterIds = bundle.getStringArrayList("filterIds");
            Log.e(TAG, "onCreateView: from: " + from);
            Log.e(TAG, "onCreateView: via: " + via);

        } else {
            Log.e(TAG, "onCreateView: bundle is null");
        }

        InAppBillingHelper.getInstance().initialiseBillingClient(getActivity());
        InAppBillingHelper.getInstance().establishConnection();

        layoutClose.setOnClickListener(v -> {
            if (getActivity() != null) {
                Utility.getInstance().addSplashAnimation(ivTile1, ivTile2, ivTile3, false);
                HomeActivity.isFragmentVisible = false;
                ApplyEffectActivity.isFragmentVisible = false;
                if (from != null) {
                    if (from.equalsIgnoreCase("splash")) {
                        getActivity().getSupportFragmentManager().beginTransaction().remove(FreeTrailFragment.this).commit();
                        fragmentClickListener.itemClicked();

                    } else if (from.equalsIgnoreCase("home")) {
                        requireActivity().getSupportFragmentManager().popBackStack();
                        HomeActivity.isFragmentVisible = false;

                    } else {
                        requireActivity().getSupportFragmentManager().popBackStack();
                        HomeActivity.isFragmentVisible = true;
                    }
                }
            } else {
                Log.e(TAG, "onCreateView: from is null");
            }
        });

        buySubBtn.setOnClickListener(v -> {
            if (ConnectionManager.getInstance().isNetworkAvailable(requireActivity())) {
                if (InAppBillingHelper.getInstance().isConnectionEstablished()) {
                    InAppBillingHelper.getInstance().GetSubPurchases(getActivity());
                    Log.e(TAG, "click listener: purchase value: " + Constants.PURCHASE_VAL);
                    isRefresh = true;

                } else {
                    Log.e(TAG, "onCreateView: connection not established");
                }
            } else {
                Toast.makeText(requireActivity(), getString(R.string.internet_con_msg), Toast.LENGTH_SHORT).show();
            }
        });

        tvManageSub.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions"));
            try {
                startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "getDeviceInfo: exception: " + e.getLocalizedMessage());
                Toast.makeText(getActivity(), R.string.con_error_msg, Toast.LENGTH_SHORT).show();
            }
            startActivity(browserIntent);
        });

        tvPrivacyPolicy.setOnClickListener(v -> {
            Uri webpage = Uri.parse("https://airportflightsstatus.com/");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(browserIntent);
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: frag");
        Log.e(TAG, "onResume: refresh val: " + isRefresh);
        Log.e(TAG, "onResume: from: " + from);
        Log.e(TAG, "onResume: via: " + via);
        Log.e(TAG, "onResume: purchase value: " + Constants.PURCHASE_VAL);

        HomeActivity.existingFragment = this;

    }

    @Override
    public void onPremiumStatusChanged(boolean isPremium) {
        Log.e(TAG, "onPremiumStatusChanged: is premium: " + isPremium);
        isBought = isPremium;
        if (isRefresh && isBought) {
            Log.e(TAG, "onResume: from: " + from);

            if (!from.isEmpty() && from.equalsIgnoreCase("home") || !from.isEmpty() && from.equalsIgnoreCase("splash")) {
                Log.e(TAG, "onResume: working home");
                startActivity(new Intent(requireActivity(), HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            } else if (!from.isEmpty() && from.equalsIgnoreCase("filter")) {
                Log.e(TAG, "onResume: working filter condition");
                Intent intent = new Intent(requireActivity(), HomeActivity.class);
                intent.putExtra("from", "trail");
                intent.putExtra("via", via);
                intent.putStringArrayListExtra("filterIds", filterIds);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (!from.isEmpty() && from.equalsIgnoreCase("exit")) {
                Log.e(TAG, "onResume: working filter condition");
                Intent intent = new Intent(requireActivity(), HomeActivity.class);
                intent.putExtra("from", from);
                intent.putExtra("via", via);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else if (!from.isEmpty() && from.equalsIgnoreCase("savedfile")) {
                Log.e(TAG, "onResume: working savedfile");
                Intent intent = new Intent(requireActivity(), SavedFileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }
}