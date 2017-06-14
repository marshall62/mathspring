function toggleVolumeControl() {
    console.log('toggle');
    if (! window.volumeControlOpen) {
        $('.slider-wrapper').css('display', 'block');
        window.volumeControlOpen = true;
    } else {
        $('.slider-wrapper').css('display', 'none');
        window.volumeControlOpen = false;
    }
}

function closeVolumeControl(e) {
    console.log('close');
    var volumeEl = $('.slider-wrapper');
    if (!volumeEl.is(e.target) && volumeEl.has(e.target).length === 0) {
        volumeEl.hide();
        window.volumeControlOpen = false;
        console.log('inside');
    }
}

function adjustVolume() {
    $('#backgroundmusic').volume = 0.5;
}