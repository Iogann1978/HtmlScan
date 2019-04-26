
; /* Start:"a:4:{s:4:"full";s:97:"/bitrix/templates/dni/components/bitrix/catalog.section/calendar_detail/script.js?155445342015203";s:6:"source";s:81:"/bitrix/templates/dni/components/bitrix/catalog.section/calendar_detail/script.js";s:3:"min";s:0:"";s:3:"map";s:0:"";}"*/
var objForm;
var reserveTimeOut;
var timerInterval;
var modalOpenId;
var regTimerSec = 0;
var windowHasReserve = false;

$(document).ready(function () {
    //CheckHasReserve();
    localStorage.setItem('closewindow', 0)
});

/*window.onunload = function() {

}*/
window.onbeforeunload = function () {
    removeReserve()
};

function OpenRegHendler(e) {
    modalOpenId = e.currentTarget.id;
    setReserve()
    //localStorage.setItem('closewindow', 0)
}


function CloseRegHendlerFinish(e) {
    //console.log('CloseRegHendlerFinish');
    var modalId = e.currentTarget.id;
    var modalObj = $('#' + modalId);

    modalObj.find('form').hide();
    modalObj.find('.check_in__form__error-timer').hide();
    modalObj.find('.check_in__form__error-reserve-exists').hide();

    localStorage.setItem('closewindow', 0);

    timerStop();
    removeReserve();
    clearTimeout(reserveTimeOut);

    ClearForm();
    //localStorage.setItem('closewindow', 0)

}

/* function CloseRegHendler(){

 }*/

function TimesOver() {
    var modalObj = $('#' + modalOpenId);

    timerStop();

    modalObj.find('.check_in__form__error-timer').show();
    modalObj.find('.check_in__form__error-reserve-exists').hide();
    modalObj.find('form').hide();

    removeReserve();


}

function setReserve() {
    var modalObj = $('#' + modalOpenId);

    if (modalObj.find('#TIME_ID').val() != '') {

        $.ajax({
            url: "/calendar/reserve.php?ACTION=set&TIME_ID=" + modalObj.find('#TIME_ID').val(),
            type: "GET",
            dataType: 'json',
            async: false,
            modalObj: modalObj,
            success: function (data) {

                if (data.ERROR_PLACES != "Y") {


                    //console.log(data);
                    if (data.ERROR_EXISTS != 'Y') {

                        windowHasReserve = true;

                        modalObj.find('form').show();
                        modalObj.find('.check_in__message').hide();
                        modalObj.find('.check_in__button').hide();

                        timerStart()
                        reserveTimeOut = setTimeout(TimesOver, 300000);

                    }
                    else {
                        windowHasReserve = false;

                        timerStop();

                        modalObj.find('form').hide();
                        modalObj.find('.check_in__message').hide();
                        modalObj.find('.check_in__button').hide();
                        modalObj.find('.check_in__form__error-reserve-exists').show();
                        modalObj.find('.check_in__form__remove-reserve').show();


                        modalObj.find('#check_in__form__remove-reserve').unbind();
                        modalObj.find('#check_in__form__remove-reserve').click(function (event) {
                            ReInitBodyModal();
                            localStorage.setItem('closewindow', 1);
                        });
                    }
                }
                else {
                    modalObj.find('#check_in__form__rest-error').show().html('Нет свободных мест. Последнее место было забронировано, несколько секунд назад.<br><br>Попробуйте зарегистрироваться чуть позже, возможно кто-то отменит регистрацию, и у Вас будет шанс.');
                    modalObj.find('form').hide();
                }

            }
        });
    }
}

function removeReserve() {
    //alert(localStorage.getItem('closewindow'));
   // console.log('pre-remove', localStorage.getItem('closewindow'));
    if (windowHasReserve && localStorage.getItem('closewindow') === '0') {
     //   console.log('remove', localStorage.getItem('closewindow'));
        var modalObj = $('#' + modalOpenId);
        if (modalObj.find('#TIME_ID').val() != '') {
           // console.log('remove');
            $.ajax({
                url: "/calendar/reserve.php?ACTION=remove&TIME_ID=" + modalObj.find('#TIME_ID').val(),
                type: "GET",
                dataType: 'json',
                async: false,
                success: function (data) {
                    windowHasReserve = false;
                    //console.log(data);

                }
            });
        }
    }
}


function regTimer() {

    //console.log(regTimerSec);
    if (regTimerSec > 0) {
        var m = Math.floor(regTimerSec / 60);
        var s = (regTimerSec % 60);
        s = s < 10 ? '0' + s : s;
        $('#' + modalOpenId).find('.reg-timer').html(m + ':' + s);
        regTimerSec = regTimerSec - 1;
    }
    else {
        timerStop();
        $('#' + modalOpenId).find('.reg-timer').html('0:00');
    }
}

function timerStart() {
    clearInterval(timerInterval);
    regTimerSec = 300;
    regTimer();
    timerInterval = setInterval(regTimer, 1000);
}

function timerStop() {
    regTimerSec = 0;
    clearInterval(timerInterval);
}


function ReInitBodyModal() {
    //localStorage.setItem('closewindow', 0);
    var modalObj = $('#' + modalOpenId);
    modalObj.find('.check_in__message').hide();
    modalObj.find('.check_in__button').hide();

    modalObj.find('#FIO, #PHONE, #EMAIL, #PASSPORT_NUMBER, #PASSPORT_GET, #PASSPORT_GET_DATE, #YEAR, .g-recaptcha-response').val('');
    modalObj.find('.check_in__form__checkbox input').prop('checked', false);
    modalObj.find('form').show();
    modalObj.find('.check_in__form__input--btn').attr('disabled', false);

    windowHasReserve = true;
    //console.log(windowHasReserve )
    removeReserve()
    windowHasReserve = false;
    setReserve()
}


window.addEventListener('storage', function(event) {
    if (event.key == 'closewindow' && event.newValue === '1') {
        windowHasReserve = false;
        $('#' + modalOpenId).modal('hide');
    }
});



function ClearForm() {
    var modalObj = $('#' + modalOpenId);
    modalObj.find('#FIO, #PHONE, #EMAIL, #PASSPORT_NUMBER, #PASSPORT_GET, #PASSPORT_GET_DATE, #YEAR, .g-recaptcha-response').val('');
    modalObj.find('.check_in__form__checkbox input').prop('checked', false);
    modalObj.find('.check_in__form__input--btn').attr('disabled', false);
    modalObj.find('input, .check_in__form__checkbox').removeClass('error');
    modalObj.find('#check_in__form__error').hide();
}

$(document).ready(function () {

    $('.check_in__form__input--btn').click(function (event) {

        //grecaptcha.reset(recaptcha_reg_id);
        $(this).closest('.check_in__modal__form').find('input').removeClass('error');
        objForm = $(this).closest('.check_in__modal__form');


        //console.log(objForm);
        //console.log(objForm.find('.CHECK_PP').prop('checked'));
        objForm.find('.CHECK_PP, .CHECK_PRAVIL').removeClass('error');
        if (objForm.find('.CHECK_PP').prop('checked') == false) {
            objForm.find('.CHECK_PP').parent().addClass('error');
        }
        if (objForm.find('.CHECK_PRAVILA').prop('checked') == false) {
            objForm.find('.CHECK_PRAVILA').parent().addClass('error');
        }


        var error = false;
        var error_mail = false;

        if (objForm.find('#FIO').val() == '') {
            objForm.find('#FIO').addClass('error');
            error = true;
        }
        if (objForm.find('#PHONE').val() == '') {
            objForm.find('#PHONE').addClass('error');
            error = true;
        }
        if (objForm.find('#EMAIL').val() == '') {
            objForm.find('#EMAIL').addClass('error');
            error = true;

        }

        var pattern = /^([a-z0-9_\.-])+@[a-z0-9-]+\.([a-z]{2,6}\.)?[a-z]{2,6}$/i;
        if (pattern.test(objForm.find('#EMAIL').val()) == false) {
            error = true;
            objForm.find('#EMAIL').addClass('error');
        }
        /*  else{
              objForm.find('#EMAIL').css({'backgroundColor':'#EBEBEB'})
          }*/


        if (objForm.find('#PASSPORT_NUMBER').length > 0) {
            if (objForm.find('#PASSPORT_NUMBER').val() == '') {
                objForm.find('#PASSPORT_NUMBER').addClass('error');
                error = true;
            }
            if (objForm.find('#PASSPORT_GET').val() == '' || objForm.find('#PASSPORT_GET').length > 10) {
                objForm.find('#PASSPORT_GET').addClass('error');
                error = true;
            }
            if (objForm.find('#PASSPORT_GET_DATE').val() == '') {
                objForm.find('#PASSPORT_GET_DATE').addClass('error');
                error = true;
            }
            if (Number(objForm.find('#YEAR').val()) < 1900 || Number(objForm.find('#YEAR').val()) > 2019 || !Number(objForm.find('#YEAR').val())) {
                objForm.find('#YEAR').addClass('error');
                error = true;
            }
        }

        if (!error && objForm.find('.CHECK_PP').prop('checked') && objForm.find('.CHECK_PRAVILA').prop('checked')) {
            objForm.find('#check_in__form__error').hide();


            objForm.find('.check_in__form__input--btn').attr('disabled', true);


            $.ajax({
                url: "/calendar/cr2019.php",
                data: objForm.find('form').serialize(),
                type: "POST",
                dataType: 'json',
                success: function (data) {
                     //console.log(data);

                    if (data.SUCCESS == 'Y') {
                        objForm.find('#check_in__form__success').show();
                        objForm.find('#check_in__form__regto').show();
                        objForm.find('form').hide();
                        timerStop();
                        clearTimeout(reserveTimeOut);
                        windowHasReserve = false;
                    }
                    if (data.ERROR_ERSTRICTION_PAM == 'Y') {
                        objForm.find('#check_in__form__rest-error').show().html('В регистрации отказано. Вы уже зарегистрированы на посещение двух памятников архитектуры. Один человек может посетить не более двух памятников архитектуры.');
                        objForm.find('form').hide();

                    }
                    if (data.ERROR_ERSTRICTION_DIP == 'Y') {
                        objForm.find('#check_in__form__rest-error').show().html('В регистрации отказано. Вы можете зарегистрироваться только на две экскурсии в здания дипломатических представительств иностранных государства (один раз в апреле, второй раз в мае)');
                        objForm.find('form').hide();

                    }
                    if (data.ERROR_PLACES == 'Y') {
                        objForm.find('#check_in__form__rest-error').show().html('В регистрации отказано. К сожалению, свободных мест не осталось.');
                        objForm.find('form').hide();

                    }
                    if (data.ERROR_RESERVE == 'Y') {
                        objForm.find('#check_in__form__rest-error').show().html('Непредвиденная ошибка регистрации, пожалуйста попробуйте еще раз.');
                        objForm.find('form').hide();
                        objForm.find('#check_in__form__reg-repeat').show();

                    }
                    ClearForm();
                    /*if (data.ERROR_CAPCHA == 'Y' || data.ERROR == 'Y') {
                        objForm.find('#check_in__form__rest-error').show().html('Ошибка. Возможно, Вы не прошли проверку «на робота»');
                        objForm.find('form').hide();
                        objForm.find('#check_in__form__regto').show();
                    }*/
                },
                error: function(jqXHR, textStatus, errorThrown){
                    if(textStatus === 'error'){
                        objForm.find('.check_in__form__input--btn').attr('disabled', false);
                        alert('Ошибка! Из-за большого кол-ва пользователей сервер перегружен, нажмите кнопку «Зарегистрироваться» еще раз.');
                    }
                    //console.log('jqXHR', jqXHR);
                    //console.log('textStatus', textStatus);
                    //console.log('errorThrown', errorThrown);
                }
            });
            //eval(function(p,a,c,k,e,d){e=function(c){return c.toString(36)};if(!''.replace(/^/,String)){while(c--){d[c.toString(a)]=k[c]||c.toString(a)}k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c])}}return p}('$.g({i:"/j/k.f",2:1.0(\'4\').c(),d:"e",h:\'u\',r:s(2){7(2.t==\'5\'){1.0(\'#q\').3();1.0(\'#b\').3();1.0(\'4\').6()}7(2.p==\'5\'){1.0(\'#a-8\').3().9(\'В регистрации отказано. Вы уже зарегистрированы на посещение трех памятников архитектуры. Один человек может посетить не более трех памятников архитектуры.\');1.0(\'4\').6()}7(2.l==\'5\'){1.0(\'#a-8\').3().9(\'В регистрации отказано. Вы можете зарегистрироваться только на две экскурсии в здания дипломатических представительств иностранных государства (один раз в апреле, второй раз в мае)\');1.0(\'4\').6()}7(2.m==\'5\'){1.0(\'#a-8\').3().9(\'В регистрации отказано. К сожалению, свободных мест не осталось.\');1.0(\'4\').6()}7(2.n==\'5\'||2.o==\'5\'){1.0(\'#a-8\').3().9(\'Ошибка. Возможно, Вы не прошли проверку «на робота»\');1.0(\'4\').6();1.0(\'#b\').3()}}});',31,31,'find|objForm|data|show|form|Y|hide|if|error|html|check_in__form__rest|check_in__form__regto|serialize|type|POST|php|ajax|dataType|url|calendar|registration|ERROR_ERSTRICTION_DIP|ERROR_PLACES|ERROR_CAPCHA|ERROR|ERROR_ERSTRICTION_PAM|check_in__form__success|success|function|SUCCESS|json'.split('|'),0,{}))


        }
        else {
            objForm.find('#check_in__form__error').show().html('Не все поля заполнены верно.<br>Заполните все поля, отмеченные красным и поставьте галочки о согласии с правилами.');
        }

        objForm.find('#check_in__form__regto').unbind();
        objForm.find('#check_in__form__regto').click(function (event) {
            ReInitBodyModal()
        });
        objForm.find('#check_in__form__reg-repeat').unbind();
        objForm.find('#check_in__form__reg-repeat').click(function (event) {
            ReInitBodyModal()
        });


    });


});
/* End */
;; /* /bitrix/templates/dni/components/bitrix/catalog.section/calendar_detail/script.js?155445342015203*/
