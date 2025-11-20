package org.me.gcu.brady_luke_s2215696;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RatesAdapter extends RecyclerView.Adapter<RatesAdapter.VH> {

    public interface OnItemClick {
        void onClick(CurrencyRate rate);
    }

    private OnItemClick click;
    private final List<CurrencyRate> data = new ArrayList<>();

    public void setOnItemClick(OnItemClick c) { this.click = c; }
    public void setData(List<CurrencyRate> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rate, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        CurrencyRate r = data.get(pos);
        h.tvCode.setText(r.code);
        h.tvRate.setText("1 GBP = " + r.rateToGBP + " " + r.code);
        h.tvName.setText(r.name);

        // ---------- FLAGS ----------
        String countryCode = getCountryCodeFromCurrency(r.code);   // e.g. "us"
        if (h.ivFlag != null) {
            int resId = h.itemView.getResources().getIdentifier(
                    countryCode.toLowerCase(),   // matches us.png, gb.png, etc.
                    "drawable",
                    h.itemView.getContext().getPackageName()
            );
            if (resId != 0) {
                h.ivFlag.setImageResource(resId);
            } else {
                h.ivFlag.setImageResource(R.drawable.unknown); // fallback image
            }
        }

        // simple colour buckets
        int bg;
        if (r.rateToGBP < 1.0) bg = 0xFF2E7D32;          // green
        else if (r.rateToGBP < 5.0) bg = 0xFF81C784;     // light green
        else if (r.rateToGBP < 10.0) bg = 0xFFFFF59D;    // yellow
        else bg = 0xFFE57373;                            // red
        h.itemView.setBackgroundColor(bg);
        h.itemView.setOnClickListener(v -> {
            if (click != null) click.onClick(r);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvCode, tvRate, tvName;
        ImageView ivFlag;
        VH(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvRate = itemView.findViewById(R.id.tvRate);
            tvName = itemView.findViewById(R.id.tvName);
            ivFlag = itemView.findViewById(R.id.ivFlag);
        }
    }
    // ---------- Currency → Country mapping ----------
    private String getCountryCodeFromCurrency(String code) {
        if (code == null) return "unknown";

        switch (code.toUpperCase()) {
            // --- Majors ---
            case "USD": return "us"; // US Dollar
            case "GBP": return "gb"; // Pound Sterling
            case "EUR": return "eu"; // Euro (use EU flag file)
            case "JPY": return "jp"; // Japanese Yen
            case "AUD": return "au"; // Australian Dollar
            case "NZD": return "nz"; // New Zealand Dollar
            case "CAD": return "ca"; // Canadian Dollar
            case "CHF": return "ch"; // Swiss Franc
            case "CNY": return "cn"; // Chinese Yuan
            case "HKD": return "hk"; // Hong Kong Dollar
            case "SGD": return "sg"; // Singapore Dollar
            case "SEK": return "se"; // Swedish Krona
            case "NOK": return "no"; // Norwegian Krone
            case "DKK": return "dk"; // Danish Krone
            // --- Europe (non-euro) ---
            case "PLN": return "pl"; // Polish Zloty
            case "CZK": return "cz"; // Czech Koruna
            case "HUF": return "hu"; // Hungarian Forint
            case "RON": return "ro"; // Romanian Leu
            case "HRK": return "hr"; // Croatian Kuna (legacy)
            case "RSD": return "rs"; // Serbian Dinar
            case "ISK": return "is"; // Icelandic Krona
            case "UAH": return "ua"; // Ukrainian Hryvnia
            case "BYN": return "by"; // Belarusian Ruble
            case "MDL": return "md"; // Moldovan Leu
            case "GEL": return "ge"; // Georgian Lari
            case "TRY": return "tr"; // Turkish Lira
            case "MKD": return "mk"; // North Macedonian Denar
            case "BGN": return "bg"; // Bulgarian Lev
            // --- Americas ---
            case "MXN": return "mx"; // Mexican Peso
            case "BRL": return "br"; // Brazilian Real
            case "ARS": return "ar"; // Argentine Peso
            case "CLP": return "cl"; // Chilean Peso
            case "COP": return "co"; // Colombian Peso
            case "PEN": return "pe"; // Peruvian Sol
            case "UYU": return "uy"; // Uruguayan Peso
            case "PYG": return "py"; // Paraguayan Guaraní
            case "BOB": return "bo"; // Boliviano
            case "DOP": return "do"; // Dominican Peso
            case "GTQ": return "gt"; // Guatemalan Quetzal
            case "HNL": return "hn"; // Honduran Lempira
            case "NIO": return "ni"; // Nicaraguan Córdoba
            case "CRC": return "cr"; // Costa Rican Colón
            case "BBD": return "bb"; // Barbados Dollar
            case "BSD": return "bs"; // Bahamian Dollar
            case "JMD": return "jm"; // Jamaican Dollar
            case "HTG": return "ht"; // Haitian Gourde
            case "TTD": return "tt"; // Trinidad and Tobago Dollar
            case "BMD": return "bm"; // Bermudian Dollar
            case "XCD": return "ag"; // East Caribbean Dollar – pick Antigua & Barbuda
            case "SRD": return "sr"; // Surinam Dollar
            // --- Middle East / Gulf ---
            case "AED": return "ae"; // UAE Dirham
            case "SAR": return "sa"; // Saudi Riyal
            case "QAR": return "qa"; // Qatari Riyal
            case "KWD": return "kw"; // Kuwaiti Dinar
            case "OMR": return "om"; // Omani Rial
            case "BHD": return "bh"; // Bahraini Dinar
            case "JOD": return "jo"; // Jordanian Dinar
            case "ILS": return "il"; // Israeli Shekel
            case "LBP": return "lb"; // Lebanese Pound
            case "SYP": return "sy"; // Syrian Pound
            case "YER": return "ye"; // Yemeni Rial
            case "IQD": return "iq"; // Iraqi Dinar
            case "IRR": return "ir"; // Iranian Rial
            // --- Africa ---
            case "ZAR": return "za"; // South African Rand
            case "EGP": return "eg"; // Egyptian Pound
            case "NGN": return "ng"; // Nigerian Naira
            case "KES": return "ke"; // Kenyan Shilling
            case "TZS": return "tz"; // Tanzanian Shilling
            case "UGX": return "ug"; // Ugandan Shilling
            case "GHS": return "gh"; // Ghanaian Cedi
            case "MAD": return "ma"; // Moroccan Dirham
            case "DZD": return "dz"; // Algerian Dinar
            case "TND": return "tn"; // Tunisian Dinar
            case "ETB": return "et"; // Ethiopian Birr
            case "MZN": return "mz"; // Mozambican Metical
            case "AOA": return "ao"; // Angolan Kwanza
            case "BWP": return "bw"; // Botswana Pula
            case "MUR": return "mu"; // Mauritian Rupee
            case "SCR": return "sc"; // Seychelles Rupee
            case "NAD": return "na"; // Namibian Dollar
            case "ZMW": return "zm"; // Zambian Kwacha
            case "MWK": return "mw"; // Malawian Kwacha
            case "RWF": return "rw"; // Rwandan Franc
            case "BIF": return "bi"; // Burundian Franc
            case "CDF": return "cd"; // Congolese Franc
            case "SOS": return "so"; // Somali Shilling
            case "LYD": return "ly"; // Libyan Dinar
            case "SDG": return "sd"; // Sudanese Pound
            case "XOF": return "sn"; // West African CFA – pick Senegal
            case "XAF": return "cm"; // Central African CFA – pick Cameroon
            // --- Asia (further) ---
            case "THB": return "th"; // Thai Baht
            case "INR": return "in"; // Indian Rupee
            case "PKR": return "pk"; // Pakistani Rupee
            case "BDT": return "bd"; // Bangladeshi Taka
            case "LKR": return "lk"; // Sri Lankan Rupee
            case "MMK": return "mm"; // Myanmar Kyat
            case "VND": return "vn"; // Vietnamese Dong
            case "IDR": return "id"; // Indonesian Rupiah
            case "PHP": return "ph"; // Philippine Peso
            case "MYR": return "my"; // Malaysian Ringgit
            case "KHR": return "kh"; // Cambodian Riel
            case "LAK": return "la"; // Lao Kip
            case "MNT": return "mn"; // Mongolian Tögrög
            case "KZT": return "kz"; // Kazakhstani Tenge
            case "UZS": return "uz"; // Uzbekistani Som
            case "TJS": return "tj"; // Tajikistani Somoni
            case "AFN": return "af"; // Afghan Afghani
            case "NPR": return "np"; // Nepalese Rupee
            case "BND": return "bn"; // Brunei Dollar
            case "TWD": return "tw"; // New Taiwan Dollar
            // --- Oceania / Pacific islands ---
            case "FJD": return "fj"; // Fijian Dollar
            case "PGK": return "pg"; // Papua New Guinean Kina
            case "WST": return "ws"; // Samoan Tala
            case "TOP": return "to"; // Tongan Paʻanga
            case "VUV": return "vu"; // Vanuatu Vatu
            case "SBD": return "sb"; // Solomon Islands Dollar
            case "XPF": return "pf"; // CFP Franc – French Polynesia
            // --- Caribbean / small ---
            case "ANG": return "cw"; // Netherlands Antillean Guilder – use Curaçao
            case "AWG": return "aw"; // Aruban Florin
            case "BZD": return "bz"; // Belize Dollar
            case "CUP": return "cu"; // Cuban Peso
            case "CUC": return "cu"; // Cuban Convertible Peso (legacy)
            case "GYD": return "gy"; // Guyana Dollar
            // --- Random others often in feeds ---
            case "BAM": return "ba"; // Bosnia & Herzegovina Convertible Mark
            case "MGA": return "mg"; // Malagasy Ariary
            case "LSL": return "ls"; // Lesotho Loti
            case "SZL": return "sz"; // Eswatini Lilangeni
            case "MOP": return "mo"; // Macanese Pataca
            case "ARSN": return "ar"; // if you see weird variants

            // --- If something slips through we didn't list ---
            default:
                return "unknown";
        }
    }}
