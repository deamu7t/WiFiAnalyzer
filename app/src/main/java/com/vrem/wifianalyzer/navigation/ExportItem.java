/*
 * WiFi Analyzer
 * Copyright (C) 2016  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.navigation;

import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;

import com.vrem.wifianalyzer.MainActivity;
import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.model.WiFiSignal;

import java.util.List;

class ExportItem implements NavigationMenuItem {

    @Override
    public void activate(@NonNull MainActivity mainActivity, @NonNull MenuItem menuItem, @NonNull NavigationMenu navigationMenu) {
        String title = getTitle(mainActivity);
        List<WiFiDetail> wiFiDetails = getWiFiDetails();
        if (!dataAvailable(wiFiDetails)) {
            Toast.makeText(mainActivity, R.string.no_data, Toast.LENGTH_LONG).show();
            return;
        }
        String data = getData(wiFiDetails);
        Intent intent = createIntent(title, data);
        Intent chooser = createChooserIntent(intent, title);
        if (!exportAvailable(mainActivity, chooser)) {
            Toast.makeText(mainActivity, R.string.export_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        mainActivity.startActivity(chooser);
    }

    private boolean exportAvailable(@NonNull MainActivity mainActivity, @NonNull Intent chooser) {
        return chooser.resolveActivity(mainActivity.getPackageManager()) != null;
    }

    private boolean dataAvailable(@NonNull List<WiFiDetail> wiFiDetails) {
        return !wiFiDetails.isEmpty();
    }

    String getData(@NonNull List<WiFiDetail> wiFiDetails) {
        StringBuilder result = new StringBuilder();
        for (WiFiDetail wiFiDetail : wiFiDetails) {
            WiFiSignal wiFiSignal = wiFiDetail.getWiFiSignal();
            result.append(String.format("%s|%s|%ddBm|%d|%d%s|%d|%d%s|%d%s (%d - %d)|%.1fm|%s%s",
                wiFiDetail.getSSID(),
                wiFiDetail.getBSSID(),
                wiFiSignal.getLevel(),
                wiFiSignal.getPrimaryWiFiChannel().getChannel(),
                wiFiSignal.getPrimaryFrequency(),
                WifiInfo.FREQUENCY_UNITS,
                wiFiSignal.getCenterWiFiChannel().getChannel(),
                wiFiSignal.getCenterFrequency(),
                WifiInfo.FREQUENCY_UNITS,
                wiFiSignal.getWiFiWidth().getFrequencyWidth(),
                WifiInfo.FREQUENCY_UNITS,
                wiFiSignal.getFrequencyStart(),
                wiFiSignal.getFrequencyEnd(),
                wiFiSignal.getDistance(),
                wiFiDetail.getCapabilities(),
                System.lineSeparator()));
        }
        return result.toString();
    }


    private List<WiFiDetail> getWiFiDetails() {
        return MainContext.INSTANCE.getScanner().getWiFiData().getWiFiDetails();
    }

    @NonNull
    private String getTitle(@NonNull MainActivity mainActivity) {
        Resources resources = mainActivity.getResources();
        return resources.getString(R.string.action_access_points);
    }

    private Intent createIntent(String title, String data) {
        Intent intent = createSendIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, data);
        return intent;
    }

    Intent createSendIntent() {
        return new Intent(Intent.ACTION_SEND);
    }

    Intent createChooserIntent(@NonNull Intent intent, @NonNull String title) {
        return Intent.createChooser(intent, title);
    }

}
