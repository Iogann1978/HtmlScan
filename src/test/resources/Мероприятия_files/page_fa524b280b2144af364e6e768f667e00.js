
; /* Start:"a:4:{s:4:"full";s:89:"/bitrix/templates/dni/components/bitrix/catalog.section/calendar/script.js?15541098852714";s:6:"source";s:74:"/bitrix/templates/dni/components/bitrix/catalog.section/calendar/script.js";s:3:"min";s:0:"";s:3:"map";s:0:"";}"*/
var posTopComplCalc;
var heightComplCalc;
var heightComplCalcDiv;
var delta;
var outerWidth_;
$(document).ready(function () {
    posTopComplCalc = $('.calendar-left__wrp').offset().top - 30;
    heightComplCalc = $('.calendar-left__wrp').outerHeight();
    heightComplCalcDiv = $('.calendar-left__wrp > div').outerHeight() + 110;
    outerWidth_ = $(document).outerWidth();
});
$(window).resize(function () {
    posTopComplCalc = $('.calendar-left__wrp').offset().top - 30;
    heightComplCalc = $('.calendar-left__wrp').outerHeight();
    heightComplCalcDiv = $('.calendar-left__wrp > div').outerHeight() + 110;
    outerWidth_ = $(document).outerWidth();
    $('.calendar-left__wrp').css({'display': 'block'});
    $('.calendar-left__wrp > div').css({'transform': 'translateY(0px)'})
});
$(window).scroll(function () {
    /*heightComplCalc = $('.calendar-left__wrp').outerHeight();
    heightComplCalcDiv = $('.calendar-left__wrp > div').outerHeight() + 110;*/
    var st = $(this).scrollTop();
    if (outerWidth_ >= 1024) {
        if (st > posTopComplCalc) {
            if ((posTopComplCalc + heightComplCalc) > (st + heightComplCalcDiv)) {
                delta = st - posTopComplCalc;
                //$('.calendar-left__wrp').css({'display': 'block'});
                $('.calendar-left__wrp > div').css({'transform': 'translateY(' + delta + 'px)'})
            }
            else {
                var StopTop = heightComplCalc - (heightComplCalc + 110);
                //$('.calendar-left__wrp').css({'display': 'block'});
                $('.calendar-left__wrp > div').css({'transform': 'translateY(' + StopTop + 'px)'});
            }
        }
        else {
            //$('.calendar-left__wrp').css({'display': 'block'});
            $('.calendar-left__wrp > div').css({'transform': 'translateY(0px)'})
        }
    }
    else {
        //$('.calendar-left__wrp').css({'display': 'block'});
        $('.calendar-left__wrp > div').css({'transform:': 'translateY(0px)'})
    }
});

/*
$(document).ready(function () {
    $('.events_gallery__spec-day__item').click(function(){
        $('.events_gallery__item__col').addClass('hidden-xs-up');
        $('.events_gallery__spec-day__item').addClass('hidden-xs-up');
        $('.events_gallery__spec__show-days').parent().removeClass('hidden-xs-up');
        $('[data-second-type="'+$(this).attr('data-spec')+'"]').removeClass('hidden-xs-up');
    });
    $('.events_gallery__spec__show-days').click(function(){
        $('.events_gallery__spec-day__item').removeClass('hidden-xs-up');
        $('.events_gallery__item__col').addClass('hidden-xs-up');
        $('.events_gallery__spec__show-days').parent().addClass('hidden-xs-up');
    });
});*/

/* End */
;; /* /bitrix/templates/dni/components/bitrix/catalog.section/calendar/script.js?15541098852714*/
