/*!
 * SweetDropdown: Sweet and versatile dropdowns
 * v1.0.0, 2016-06-02
 * http://github.com/adeptoas/sweet-dropdown
 *
 * Copyright (c) 2016 Adepto.as AS · Oslo, Norway
 * Dual licensed under the MIT and GPL licenses.
 *
 * See LICENSE-MIT.txt and LICENSE-GPL.txt
 */

/**
 * SweetDropdown
 * Sweet and versatile jQuery dropdown plugin
 *
 * @author  bluefirex
 * @version  1.0
 */
(function($) {

  /**
  	 * jQuery Element Binding
  	 * Targets a trigger and connects it to a dropdown menu. Allows for chaining.
  	 *
  	 * @param  {string} method Method to run: attach, detach, show, hide, enable, disable
  	 * @param  {mixed}  data   Data for the method
   */
  var showDropdown;
  $.fn.sweetDropdown = function(method, data) {
    switch (method) {
      case 'attach':
        return $(this).attr('data-dropdown', data);
      case 'detach':
        return $(this).removeAttr('data-dropdown');
      case 'show':
        return $(this).click();
      case 'hide':
        $.sweetDropdown.hideAll();
        return $(this);
      case 'enable':
        return $(this).removeClass('dropdown-disabled');
      case 'disable':
        return $(this).addClass('dropdown-disabled');
    }
  };

  /**
  	 * Dummy function as base for the other functions.
  	 * Doesnt do ANYTHING.
   */
  $.sweetDropdown = function() {};

  /**
  	 * Attach all dropdowns to their triggers.
   */
  $.sweetDropdown.attachAll = function() {
    $('body').off('click.dropdown').on('click.dropdown', '[data-dropdown]', showDropdown);
    $('[data-dropdown]').off('click.dropdown').on('click.dropdown', showDropdown);
    $('html, .sweet-modal-content').off('click.dropdown').on('click.dropdown', $.sweetDropdown.hideAll);
    $(window).off('resize.dropdown').on('resize.dropdown', $.sweetDropdown.hideAll);
    return true;
  };

  /**
  	 * Hide all dropdowns.
  	 *
  	 * @param  {Event}  e              Native Browser-Event
  	 * @param  {string} hideException  ID of a dropdown that should NOT be closed
   */
  $.sweetDropdown.hideAll = function(e, hideException) {
    var animTimeout, el, hideExceptionID, targetGroup, trigger;
    if (e == null) {
      e = null;
    }
    if (hideException == null) {
      hideException = null;
    }
    targetGroup = e ? $(e.target).parents().addBack() : null;
    if (targetGroup && targetGroup.hasClass('dropdown-menu') && !targetGroup.is('A')) {
      return;
    }
    el = '.dropdown-menu';
    trigger = '[data-dropdown]';
    hideExceptionID = '';
    if (hideException) {
      hideExceptionID = $(hideException).attr('id');
      if (!$('[data-dropdown="#' + hideExceptionID + '"]').hasClass('dropdown-open')) {
        el = '.dropdown-menu:not(#' + hideExceptionID + ')';
        trigger = '[data-dropdown!="#' + hideExceptionID + '"]';
      }
    }
    $('body').find(el).removeClass('dropdown-opened').end().find(trigger).removeClass('dropdown-open');
    animTimeout = window.setTimeout(function() {
      return $('body').find(el).hide().end();
    }, 200);
    return true;
  };

  /**
  	 * All possible anchor positions.
  	 *
  	 * @type {Array}
   */
  $.sweetDropdown.ANCHOR_POSITIONS = ['top-left', 'top-center', 'top-right', 'right-top', 'right-center', 'right-bottom', 'bottom-left', 'bottom-center', 'bottom-right', 'left-top', 'left-center', 'left-bottom'];

  /**
  	 * Default settings
  	 *
  	 * @type {Object}
   */
  $.sweetDropdown.defaults = {
    anchorPosition: 'center'
  };

  /**
  	 * Show a dropdown. This is triggered by a jQuery click listener.
  	 *
  	 * @param  {Event} e    Native Browser-Event
   */
  showDropdown = function(e) {
    var $anchor, $dropdown, $trigger, addAnchorX, addAnchorY, addX, addY, anchorPosition, anchorSide, bottomTrigger, hasAnchor, heightDropdown, heightTrigger, i, isDisabled, isOpen, left, leftTrigger, len, position, positionParts, ref, rightTrigger, top, topTrigger, widthDropdown, widthTrigger;
    if (e == null) {
      e = null;
    }
    $trigger = $(this);
    $dropdown = $($trigger.data('dropdown'));
    $anchor = $dropdown.find('.dropdown-anchor');
    hasAnchor = $dropdown.hasClass('dropdown-has-anchor');
    isOpen = $trigger.hasClass('dropdown-open');
    isDisabled = $trigger.hasClass('dropdown-disabled');
    widthDropdown = $dropdown.outerWidth();
    widthTrigger = $trigger.outerWidth();
    heightDropdown = $dropdown.outerHeight();
    heightTrigger = $trigger.outerHeight();
    topTrigger = $trigger.position().top;
    leftTrigger = $trigger.position().left;
    if ($trigger.hasClass('dropdown-use-offset')) {
      topTrigger = $trigger.offset().top;
      leftTrigger = $trigger.offset().left;
    }
    bottomTrigger = topTrigger + heightTrigger;
    rightTrigger = leftTrigger + widthTrigger;
    if ($dropdown.length < 1) {
      return console.error('[SweetDropdown] Could not find dropdown: ' + $(this).data('dropdown'));
    }
    if ($anchor.length < 1 && hasAnchor) {
      $anchor = $('<div class="dropdown-anchor"></div>');
      $dropdown.prepend($anchor);
    }
    if (e !== void 0) {
      e.preventDefault();
      e.stopPropagation();
    }
    if (isOpen || isDisabled) {
      return false;
    }
    $.sweetDropdown.hideAll(null, $trigger.data('dropdown'));
    anchorPosition = $.sweetDropdown.defaults.anchorPosition;
    ref = $.sweetDropdown.ANCHOR_POSITIONS;
    for (i = 0, len = ref.length; i < len; i++) {
      position = ref[i];
      if ($dropdown.hasClass('dropdown-anchor-' + position)) {
        anchorPosition = position;
      }
    }
    top = 0;
    left = 0;
    positionParts = anchorPosition.split('-');
    anchorSide = positionParts[0];
    anchorPosition = positionParts[1];
    if (anchorSide === 'top' || anchorSide === 'bottom') {
      switch (anchorPosition) {
        case 'left':
          left = leftTrigger;
          break;
        case 'center':
          left = leftTrigger - widthDropdown / 2 + widthTrigger / 2;
          break;
        case 'right':
          left = rightTrigger - widthDropdown;
      }
    }
    if (anchorSide === 'left' || anchorSide === 'right') {
      switch (anchorPosition) {
        case 'top':
          top = topTrigger;
          break;
        case 'center':
          top = topTrigger - heightDropdown / 2 + heightTrigger / 2;
          break;
        case 'bottom':
          top = topTrigger + heightTrigger - heightDropdown;
      }
    }
    switch (anchorSide) {
      case 'top':
        top = topTrigger + heightTrigger;
        break;
      case 'right':
        left = leftTrigger - widthDropdown;
        break;
      case 'bottom':
        top = topTrigger - heightDropdown;
        break;
      case 'left':
        left = leftTrigger + widthTrigger;
    }
    addX = parseInt($dropdown.data('add-x'));
    addY = parseInt($dropdown.data('add-y'));
    if (!isNaN(addX)) {
      left += addX;
    }
    if (!isNaN(addY)) {
      top += addY;
    }
    addAnchorX = parseInt($trigger.data('add-anchor-x'));
    addAnchorY = parseInt($trigger.data('add-anchor-y'));
    if (!isNaN(addAnchorX)) {
      $anchor.css({
        marginLeft: addAnchorX
      });
    }
    if (!isNaN(addAnchorY)) {
      $anchor.css({
        marginTop: addAnchorY
      });
    }
    $dropdown.css({
      top: top,
      left: left,
      display: 'block'
    });
    window.setTimeout(function() {
      return $dropdown.addClass('dropdown-opened');
    }, 0);
    $trigger.addClass('dropdown-open');
    return $trigger;
  };
  return $(function() {
    return $.sweetDropdown.attachAll();
  });
})(jQuery);
