=== Sytbay Payments for LearnPress ===
Tags: learnpress, paynow, zimbabwe, payment gateway, lms
Requires at least: 6.0
Tested up to: 7.1
Requires PHP: 7.4
Requires Plugins: learnpress
Stable tag: 1.0.0
License: GPLv2 or later
License URI: https://www.gnu.org/licenses/gpl-2.0.html

Accept Paynow Zimbabwe hosted payments for LearnPress courses and enrol students automatically after payment verification.

== Description ==

Sytbay Payments for LearnPress adds Paynow Zimbabwe as a payment gateway for LearnPress. It redirects the learner to Paynow's secure hosted payment page, verifies the signed payment result, checks the saved Paynow transaction status, and completes the LearnPress order only after verification succeeds.

This plugin is intended for websites that sell LearnPress courses in Zimbabwe.

**Features**

* Paynow Zimbabwe hosted checkout.
* Automatic LearnPress course enrolment after a verified successful payment.
* Signed callback validation and server-side status polling before an order is completed.
* Test mode for new Paynow integrations.
* No bundled SDK, tracking, advertising, or remote code.

This is an independent integration. It is not affiliated with, endorsed by, or maintained by Paynow Zimbabwe or LearnPress.

== Installation ==

1. Install and activate LearnPress.
2. Upload and activate Sytbay Payments for LearnPress.
3. Go to **LearnPress > Settings > Payments > Paynow Zimbabwe**.
4. Enable the gateway and enter the Integration ID and Integration Key from your Paynow merchant dashboard.
5. While Paynow has your integration in test mode, enable **Test mode** here and enter the email address registered on the Paynow merchant account.
6. Assign a non-zero price to the relevant LearnPress courses.
7. Complete a test transaction. When Paynow activates live payments, disable Test mode before accepting real payments.

== Frequently Asked Questions ==

= Does this plugin require WooCommerce? =

No. It is a direct LearnPress gateway.

= When is a student enrolled? =

Only after Paynow sends a signed payment update and the plugin independently confirms the saved transaction status with Paynow.

= Does it support EcoCash? =

Paynow controls the payment methods available on its hosted checkout. Enable the methods you need in your Paynow merchant setup.

= Does it support Paynow Express Checkout? =

No. Version 1.0.0 uses Paynow's hosted checkout only.

= Where can I find Paynow credentials? =

Create and configure a Paynow merchant integration, then copy its Integration ID and Integration Key into the gateway settings. Your Paynow account must be approved for live settlement before you accept live payments.

== Privacy ==

The plugin does not collect analytics or send data to the developer.

When a customer chooses Paynow at checkout, the site sends the Paynow Integration ID, order reference, order amount, customer name, customer email address, and callback URLs to Paynow so that Paynow can process and verify the payment. The plugin stores the Paynow reference, payment amount, and a Paynow status-poll URL on the existing LearnPress order. Refer to Paynow's privacy documentation and terms for its handling of this data.

== Changelog ==

= 1.0.0 =

* First public release.
* Adds Paynow Zimbabwe hosted checkout for LearnPress.
* Verifies signed payment callbacks and polls Paynow before completing an order.

== Upgrade Notice ==

= 1.0.0 =

First public release.
