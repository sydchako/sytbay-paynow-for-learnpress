<?php

defined( 'ABSPATH' ) || exit;

/**
 * Paynow Zimbabwe hosted-payment gateway for LearnPress.
 *
 * This implementation deliberately uses WordPress HTTP functions rather than
 * a bundled SDK, keeping it small and compatible with shared hosting.
 */
class Sytbay_LP_Gateway_Paynow extends LP_Gateway_Abstract {
	public $id = 'paynow';

	private const INITIATE_URL = 'https://www.paynow.co.zw/interface/initiatetransaction';
	private const META_REFERENCE = '_sytbay_paynow_reference';
	private const META_POLL_URL = '_sytbay_paynow_poll_url';
	private const META_AMOUNT = '_sytbay_paynow_amount';
	private const META_PAYNOW_REFERENCE = '_sytbay_paynow_reference_number';

	public function __construct() {
		$this->method_title       = __( 'Paynow Zimbabwe', 'sytbay-paynow-for-learnpress' );
		$this->method_description = __( 'Accept secure hosted payments through Paynow Zimbabwe.', 'sytbay-paynow-for-learnpress' );
		$this->title              = __( 'Paynow Zimbabwe', 'sytbay-paynow-for-learnpress' );
		$this->description        = __( 'Pay securely using Paynow.', 'sytbay-paynow-for-learnpress' );

		parent::__construct();
		$this->title       = (string) $this->settings->get( 'title', $this->title );
		$this->description = (string) $this->settings->get( 'description', $this->description );

		add_action( 'rest_api_init', array( $this, 'register_result_route' ) );
	}

	public function get_settings(): array {
		return array(
			array( 'type' => 'title' ),
			array(
				'title'   => __( 'Enable/Disable', 'sytbay-paynow-for-learnpress' ),
				'id'      => '[enable]',
				'default' => 'no',
				'type'    => 'checkbox',
				'desc'    => __( 'Enable Paynow Zimbabwe', 'sytbay-paynow-for-learnpress' ),
			),
			array(
				'title' => __( 'Integration ID', 'sytbay-paynow-for-learnpress' ),
				'id'    => '[integration_id]',
				'type'  => 'text',
				'desc'  => __( 'From your Paynow merchant dashboard.', 'sytbay-paynow-for-learnpress' ),
			),
			array(
				'title' => __( 'Integration Key', 'sytbay-paynow-for-learnpress' ),
				'id'    => '[integration_key]',
				'type'  => 'password',
				'desc'  => __( 'Keep this secret. It is used to sign and verify Paynow messages.', 'sytbay-paynow-for-learnpress' ),
			),
			array(
				'title'   => __( 'Test mode', 'sytbay-paynow-for-learnpress' ),
				'id'      => '[test_mode]',
				'default' => 'no',
				'type'    => 'checkbox',
				'desc'    => __( 'Use only while Paynow keeps this integration in test mode. Disable before accepting real payments.', 'sytbay-paynow-for-learnpress' ),
			),
			array(
				'title' => __( 'Test merchant email', 'sytbay-paynow-for-learnpress' ),
				'id'    => '[test_merchant_email]',
				'type'  => 'text',
				'desc'  => __( 'The email address registered on the Paynow merchant account. It is sent only while Test mode is enabled.', 'sytbay-paynow-for-learnpress' ),
			),
			array(
				'title'   => __( 'Gateway title', 'sytbay-paynow-for-learnpress' ),
				'id'      => '[title]',
				'default' => __( 'Paynow Zimbabwe', 'sytbay-paynow-for-learnpress' ),
				'type'    => 'text',
			),
			array(
				'title'   => __( 'Gateway description', 'sytbay-paynow-for-learnpress' ),
				'id'      => '[description]',
				'default' => __( 'Pay securely using Paynow.', 'sytbay-paynow-for-learnpress' ),
				'type'    => 'textarea',
			),
			array(
				'type' => 'sectionend',
			),
		);
	}

	public function process_payment( $order_id = 0 ): array {
		$order = new LP_Order( $order_id );
		$id    = trim( (string) $this->settings->get( 'integration_id' ) );
		$key   = trim( (string) $this->settings->get( 'integration_key' ) );

		if ( ! $id || ! $key ) {
			throw new Exception( esc_html__( 'Paynow is not configured yet. Please contact the site administrator.', 'sytbay-paynow-for-learnpress' ) );
		}

		$amount = number_format( (float) $order->get_total(), 2, '.', '' );
		if ( (float) $amount <= 0 ) {
			throw new Exception( esc_html__( 'Paynow cannot process a zero-value order.', 'sytbay-paynow-for-learnpress' ) );
		}

		$reference = sprintf( 'LP-%d-%s', $order->get_id(), wp_generate_password( 10, false, false ) );
		$is_test   = 'yes' === $this->settings->get( 'test_mode', 'no' );
		$test_email = sanitize_email( (string) $this->settings->get( 'test_merchant_email' ) );
		$email     = $is_test && $test_email ? $test_email : $this->get_order_email( $order );
		$name      = $this->get_order_name( $order );
		$return    = $this->get_return_url( $order );
		$result    = rest_url( 'sytbay-paynow/v1/result' );

		$payload = array(
			'id'             => $id,
			'reference'      => $reference,
			'amount'         => $amount,
			/* translators: %s: LearnPress order number. */
			'additionalinfo' => sprintf( __( 'LearnPress order %s', 'sytbay-paynow-for-learnpress' ), $order->get_order_number() ),
			'returnurl'      => $return,
			'resulturl'      => $result,
			'authemail'      => $email,
			'authname'       => $name,
			'status'         => 'Message',
		);
		$payload['hash'] = $this->create_hash( $payload, $key );

		$response = wp_remote_post(
			self::INITIATE_URL,
			array(
				'timeout' => 45,
				'headers' => array( 'Content-Type' => 'application/x-www-form-urlencoded' ),
				'body'    => $payload,
			)
		);

		if ( is_wp_error( $response ) ) {
			throw new Exception( esc_html__( 'Paynow could not be reached. Please try again.', 'sytbay-paynow-for-learnpress' ) );
		}

		if ( 200 !== (int) wp_remote_retrieve_response_code( $response ) ) {
			throw new Exception( esc_html__( 'Paynow could not start this payment. Please try again.', 'sytbay-paynow-for-learnpress' ) );
		}

		$data = $this->parse_message( wp_remote_retrieve_body( $response ) );
		if ( empty( $data['status'] ) || 'ok' !== strtolower( $data['status'] ) || empty( $data['browserurl'] ) || empty( $data['pollurl'] ) ) {
			$message = ! empty( $data['error'] )
				? esc_html( sanitize_text_field( $data['error'] ) )
				: esc_html__( 'Paynow could not start this payment.', 'sytbay-paynow-for-learnpress' );
			throw new Exception( $message );
		}

		if ( ! $this->is_valid_hash( $data, $key ) ) {
			throw new Exception( esc_html__( 'Paynow returned an invalid payment response.', 'sytbay-paynow-for-learnpress' ) );
		}

		update_post_meta( $order->get_id(), self::META_REFERENCE, $reference );
		update_post_meta( $order->get_id(), self::META_POLL_URL, esc_url_raw( $data['pollurl'] ) );
		update_post_meta( $order->get_id(), self::META_AMOUNT, $amount );
		$order->add_note( __( 'Payment initiated through Paynow Zimbabwe.', 'sytbay-paynow-for-learnpress' ) );

		return array(
			'result'   => 'success',
			'redirect' => esc_url_raw( $data['browserurl'] ),
		);
	}

	public function register_result_route() {
		register_rest_route(
			'sytbay-paynow/v1',
			'/result',
			array(
				'methods'             => WP_REST_Server::CREATABLE,
				'callback'            => array( $this, 'handle_result' ),
				'permission_callback' => '__return_true',
			)
		);
	}

	public function handle_result( WP_REST_Request $request ) {
		$key  = trim( (string) $this->settings->get( 'integration_key' ) );
		$data = $this->parse_message( $request->get_body() );

		if ( ! $key || ! $this->is_valid_hash( $data, $key ) ) {
			return new WP_REST_Response( array( 'received' => false ), 400 );
		}

		$order = $this->find_order_by_reference( $data['reference'] ?? '' );
		if ( ! $order || ! $this->matches_order( $order, $data ) ) {
			return new WP_REST_Response( array( 'received' => false ), 404 );
		}

		/* Paynow recommends polling on important result updates. */
		$verified = $this->poll_and_verify( $order, $key );
		if ( is_wp_error( $verified ) ) {
			return new WP_REST_Response( array( 'received' => true, 'verified' => false ), 202 );
		}

		$this->apply_payment_status( $order, $verified );
		return new WP_REST_Response( array( 'received' => true ), 200 );
	}

	private function poll_and_verify( LP_Order $order, string $key ) {
		$poll_url = (string) get_post_meta( $order->get_id(), self::META_POLL_URL, true );
		if ( ! $poll_url || 0 !== strpos( $poll_url, 'https://www.paynow.co.zw/' ) ) {
			return new WP_Error( 'paynow_missing_poll_url' );
		}

		$response = wp_remote_get( $poll_url, array( 'timeout' => 45 ) );
		if ( is_wp_error( $response ) ) {
			return $response;
		}

		if ( 200 !== (int) wp_remote_retrieve_response_code( $response ) ) {
			return new WP_Error( 'paynow_poll_failed' );
		}

		$data = $this->parse_message( wp_remote_retrieve_body( $response ) );
		if ( ! $this->is_valid_hash( $data, $key ) || ! $this->matches_order( $order, $data ) ) {
			return new WP_Error( 'paynow_verification_failed' );
		}

		return $data;
	}

	private function apply_payment_status( LP_Order $order, array $data ) {
		$status = strtolower( trim( (string) ( $data['status'] ?? '' ) ) );
		$paynow_reference = sanitize_text_field( (string) ( $data['paynowreference'] ?? '' ) );

		if ( in_array( $status, array( 'paid', 'awaiting delivery', 'delivered' ), true ) ) {
			if ( ! $order->is_completed() ) {
				update_post_meta( $order->get_id(), self::META_PAYNOW_REFERENCE, $paynow_reference );
				$order->payment_complete( $paynow_reference );
				$order->add_note( __( 'Paynow payment verified and course enrolment completed.', 'sytbay-paynow-for-learnpress' ) );
			}
			return;
		}

		if ( in_array( $status, array( 'cancelled', 'refunded', 'disputed' ), true ) && $order->has_status( array( 'pending', 'processing' ) ) ) {
			$order->update_status( 'cancelled' );
			/* translators: %s: Paynow payment status. */
			$order->add_note( sprintf( __( 'Paynow payment status: %s.', 'sytbay-paynow-for-learnpress' ), ucfirst( $status ) ) );
		}
	}

	private function matches_order( LP_Order $order, array $data ): bool {
		$reference = (string) get_post_meta( $order->get_id(), self::META_REFERENCE, true );
		$amount    = (string) get_post_meta( $order->get_id(), self::META_AMOUNT, true );

		return $reference && isset( $data['reference'], $data['amount'] )
			&& hash_equals( $reference, (string) $data['reference'] )
			&& hash_equals( $amount, number_format( (float) $data['amount'], 2, '.', '' ) );
	}

	private function find_order_by_reference( string $reference ) {
		$orders = get_posts(
			array(
				'post_type'      => 'lp_order',
				'post_status'    => 'any',
				'posts_per_page' => 1,
				'fields'         => 'ids',
				// phpcs:ignore WordPress.DB.SlowDBQuery.slow_db_query_meta_query -- Paynow sends only the stored payment reference.
				'meta_query'     => array(
					array(
						'key'   => self::META_REFERENCE,
						'value' => $reference,
					),
				),
			)
		);

		return $orders ? new LP_Order( (int) $orders[0] ) : false;
	}

	private function create_hash( array $values, string $key ): string {
		$string = '';
		foreach ( $values as $name => $value ) {
			if ( 'hash' !== strtolower( $name ) ) {
				$string .= (string) $value;
			}
		}

		return strtoupper( hash( 'sha512', $string . $key ) );
	}

	private function is_valid_hash( array $values, string $key ): bool {
		if ( empty( $values['hash'] ) ) {
			return false;
		}

		return hash_equals( $this->create_hash( $values, $key ), strtoupper( (string) $values['hash'] ) );
	}

	private function parse_message( string $body ): array {
		$data = array();
		wp_parse_str( $body, $data );
		return array_change_key_case( array_map( 'wp_unslash', $data ), CASE_LOWER );
	}

	private function get_order_email( LP_Order $order ): string {
		$user = get_user_by( 'id', $order->get_user_id() );
		return $user ? sanitize_email( $user->user_email ) : '';
	}

	private function get_order_name( LP_Order $order ): string {
		$user = get_user_by( 'id', $order->get_user_id() );
		return $user ? sanitize_text_field( $user->display_name ) : get_bloginfo( 'name' );
	}
}
