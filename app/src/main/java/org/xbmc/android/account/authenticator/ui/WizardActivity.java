/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.account.authenticator.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.InjectView;
import co.juliansuarez.libwizardpager.wizard.ui.StepPagerStrip;
import org.xbmc.android.account.Constants;
import org.xbmc.android.remotesandbox.R;
import org.xbmc.android.view.RelativePagerAdapter;
import org.xbmc.android.view.RelativePagerFragment;
import org.xbmc.android.view.RelativeViewPager;
import org.xbmc.android.zeroconf.XBMCHost;

import static org.xbmc.android.account.authenticator.ui.WizardFragment.*;

public class WizardActivity extends AccountAuthenticatorActivity {

	public static final String TAG = WizardActivity.class.getSimpleName();

	public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

	private AccountManager accountManager;

	@InjectView(R.id.strip) StepPagerStrip pagerStrip;
	@InjectView(R.id.pager) RelativeViewPager pager;

	@InjectView(R.id.next_button) Button nextButton;
	@InjectView(R.id.prev_button) Button prevButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accountwizard);
		setTitle(R.string.accountwizard_title);
		ButterKnife.inject(this);

		accountManager = AccountManager.get(this);

		final RelativePagerAdapter adapter = new RelativePagerAdapter(getSupportFragmentManager());
		final WizardFragment firstPage = new Step1WelcomeFragment();
		adapter.setInitialFragment(firstPage);

		pagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
			@Override
			public void onPageStripSelected(int position) {
				position = Math.min(adapter.getCount() - 1, position);
				if (pager.getCurrentItem() != position) {
					pager.setCurrentItem(position);
				}
			}
		});
		pagerStrip.setPageCount(4);
		pagerStrip.setCurrentPage(0);

		pager.setAdapter(adapter);
		pager.setOnRelativePageChangeListener(new RelativeViewPager.OnRelativePageChangeListener() {
			@Override
			public void onPageSelected(RelativePagerFragment f) {
				final WizardFragment fragment = (WizardFragment)f;
				pagerStrip.setCurrentPage(fragment.getStep());
				updateBottomBar(fragment);
			}
		});

		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adapter.onNextPage();
			}
		});
		prevButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adapter.onPrevPage();
			}
		});

		updateBottomBar((WizardFragment)adapter.getCurrentFragment());
	}

	public void addHost(XBMCHost host) {
		Log.i(TAG, "addHost(" + host + ")");
		final Account account = new Account(host.getName(), Constants.ACCOUNT_TYPE);
		final Bundle data = new Bundle();
		data.putString(Constants.DATA_HOST, host.getHost());
		data.putString(Constants.DATA_ADDRESS, host.getAddress());
		data.putString(Constants.DATA_PORT, String.valueOf(host.getPort()));
		data.putString(Constants.DATA_USER, host.getUser());
		data.putString(Constants.DATA_PASS, host.getPass());
		accountManager.addAccountExplicitly(account, null, data);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, host.getName());
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
	}

	private void updateBottomBar(WizardFragment fragment) {
		updateButton(nextButton, fragment.hasNextButton());
		updateButton(prevButton, fragment.hasPrevButton());
		nextButton.setText(fragment.getNextButtonLabel());
		prevButton.setText(fragment.getPrevButtonLabel());
	}

	private static void updateButton(Button button, int state) {
		switch (state) {
			case STATUS_DISABLED:
				button.setVisibility(View.VISIBLE);
				button.setEnabled(false);
				break;
			case STATUS_ENABLED:
				button.setVisibility(View.VISIBLE);
				button.setEnabled(true);
				break;
			case STATUS_GONE:
				button.setVisibility(View.INVISIBLE);
				break;
		}
	}


}
