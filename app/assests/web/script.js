function logText(text) {
    Android.log(text);
}

function toBeCalledFromAndroid() {
   var result = [...document.querySelectorAll("#story_viewer_content div>img")].sort((a,b) => (a.height < b.height) ? 1 : (a.height > b.height) ? -1 : 0);
   console.log(result[0].src);
   return result[0].src;

}