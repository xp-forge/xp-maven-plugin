<?php

uses(
  'unittest.TestCase',
  'org.company.lib.common.util.Greeting'
);

/**
 * Integration tests for org.company.lib.common.util.Greeting
 *
 */
class PhpIntegrationTest extends TestCase {

  /**
   * Test constructor
   *
   */
  #[@test]
  public function should_be_able_to_create_Greeting_instances () {
    $this->assertClass(new Greeting(), 'org.company.lib.common.util.Greeting');
  }
}
