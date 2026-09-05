# Sytbay Payments for LearnPress

An independent Paynow Zimbabwe hosted-payment gateway for [LearnPress](https://wordpress.org/plugins/learnpress/).

It redirects a learner to Paynow's hosted checkout, validates Paynow's signed callback, polls Paynow to confirm the payment result, and completes the LearnPress order only after the payment is verified.

## Requirements

- WordPress 6.0 or later
- PHP 7.4 or later
- LearnPress
- A configured Paynow Zimbabwe merchant integration

## Installation

1. Install and activate LearnPress.
2. Install and activate this plugin.
3. In WordPress, open **LearnPress > Settings > Payments > Paynow Zimbabwe**.
4. Enable the gateway and enter your Paynow Integration ID and Integration Key.
5. Price the applicable courses, then complete a Paynow test transaction before accepting real payments.

## Payment safety

The plugin does not complete an order merely because a browser returns from Paynow. It verifies Paynow's signed status callback and confirms the stored transaction through Paynow before enrolling the student.

## Development and releases

This repository is the development source. The WordPress.org Plugin Directory will be the official distribution channel after approval.

## Privacy

The plugin has no analytics, advertising, or developer-operated remote service. When Paynow is selected at checkout, it sends the order reference, amount, name, email address and callback URLs to Paynow so that the payment can be processed and verified.

## Disclaimer

This is an independent integration. It is not affiliated with, endorsed by, or maintained by Paynow Zimbabwe or LearnPress.

## Licence

GPL-2.0-or-later. See [license.txt](license.txt).
