$(document).ready(function(){
    // initial selected project value
//    $('.project:first-child').addClass('selected');
//    let defaultProject=$('.project.selected').text().trim();
//    $(".selected-project-name h5").text(defaultProject);
//    $("#validate-green").attr("href",`/api/validate/green/${defaultProject}`);
//    $("#validate-blue").attr("href",`/api/validate/blue/${defaultProject}`);

    // current datetime
    let formatter={
        timeZone:'America/Denver',
        year:'numeric',
        month:'short',
        day:'numeric',
        hour:'2-digit',
        minute:'2-digit',
        hour12:true
    };

    function updateTime()
    {
        let formattedTimeMST=new Date().toLocaleString('en-US',formatter);
        $("header h6").text(formattedTimeMST);
    }

    setInterval(function(){updateTime()},1000);



    // extend api response
    $('.api').click(function(){
        $(this).toggleClass('extend');

        // rotate arrow down/up
        $(this).children('.api-header').children('.api-status').children('i').toggleClass('rotate');

        // adjust api div height to content height of api response
        let tempHeight=$(this).children('.api-response').height();

        if($(this).hasClass('extend'))
        {
            $(this).height(tempHeight+108);
        }
        else
        {
            $(this).height(51);
        }

    });

    // button click inside button loader
    $(".selected-project-action button").click(function(){
        $('.selected-project-action button').addClass('disable');
        $('.selected-project-action button').attr('disabled','disabled');
        $('.api-container .loader').addClass('show');
        $('.api').css('display','none');
        $(this).children('.icon').css('display','none');
        $(this).children('.spinner').css('display','inline-block');
    });

    // select project
    $(".project a").click(function(){
        let selectedProject=$(this).children('.project-details').children('h5').text().trim();
        $(".project.selected").removeClass("selected");
        $(this).parent(".project").addClass("selected");
        $(".selected-project-name h5").text(selectedProject);
        $("#validate-green").attr("href",`/api/validate/green/${selectedProject}`);
        $("#validate-blue").attr("href",`/api/validate/blue/${selectedProject}`);
    });

});