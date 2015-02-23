function plugin() {
    prepareForData(document);
    plug(document);
    probUtilsInit(document, isMultiChoice());
    console.log("Loaded libraries successfully");

}
