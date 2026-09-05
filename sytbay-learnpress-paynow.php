<?php
/**
 * Plugin Name: Sytbay Payments for LearnPress
 * Description: Adds Paynow Zimbabwe hosted checkout to LearnPress.
 * Version: 1.0.0
 * Requires at least: 6.0
 * Requires PHP: 7.4
 * Requires Plugins: learnpress
 * Author: Sydney Chako
 * Author URI: https://sytbay.co.zw
 * License: GPL-2.0-or-later
 * Text Domain: sytbay-paynow-for-learnpress
 */

defined( 'ABSPATH' ) || exit;

define( 'SYTBAY_LP_PAYNOW_VERSION', '1.0.0' );
define( 'SYTBAY_LP_PAYNOW_FILE', __FILE__ );
define( 'SYTBAY_LP_PAYNOW_DIR', plugin_dir_path( __FILE__ ) );

add_action( 'admin_notices', function() {
	if ( current_user_can( 'activate_plugins' ) && ! defined( 'LP_PLUGIN_FILE' ) ) {
		echo '<div class="notice notice-error"><p>' . esc_html__( 'Sytbay LearnPress Paynow Gateway requires LearnPress to be active.', 'sytbay-paynow-for-learnpress' ) . '</p></div>';
	}
} );

add_action( 'plugins_loaded', function() {
	if ( ! defined( 'LP_PLUGIN_FILE' ) ) {
		return;
	}

	require_once SYTBAY_LP_PAYNOW_DIR . 'includes/class-sytbay-lp-gateway-paynow.php';

	/* LearnPress fires this before it builds its gateway registry. */
	add_action( 'learn-press/ready', function() {
		add_filter( 'learn-press/payment-methods', function( $gateways ) {
			$gateways['paynow'] = 'Sytbay_LP_Gateway_Paynow';
			return $gateways;
		} );
	} );
}, 20 );
