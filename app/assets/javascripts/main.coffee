$ ->
  defaultData =
    width: 100
    height: 100
    files: []

  vm = ko.mapping.fromJS
    upload: defaultData
    errorText: ''
    paths: []

  $uploadForm = $('#upload-form')
  formData = null
  $uploadForm.fileupload
    dataType: 'json'
    autoUpload: no
    replaceFileInput: yes
    singleFileUploads: no
    multipart: yes
    add: (e, data) ->
      formData = data
    progressall: (e, data) ->
      console.log(data)
      progress = parseInt(data.loaded / data.total * 100, 10)
      $('#progress .bar').css('width', progress + '%')
    fail: (e, data) ->
      $('#progress').hide()
      console.log(data.jqXHR)
      if data.jqXHR?.responseText
        vm.errorText(data.jqXHR.responseText)
    done: (e, data) ->
      $('#progress').hide()
      if data.result.paths
        vm.paths.removeAll()
        for path in data.result.paths
          vm.paths.push(path)

  vm.onFilesSelected = (_, event) ->
    vm.errorText('')
    vm.upload.files.removeAll()
    for file in event.target.files
      fileName = file.name.toLowerCase()
      if !(/\.(jpg|jpeg|png)$/.test(fileName))
        vm.errorText('Only PNG or JPG files are allowed.')
        break
      else
        vm.upload.files.push(file.name)

  vm.filesInfo = ko.computed ->
    length = vm.upload.files().length
    switch length
      when 0 then 'No file chosen'
      when 1 then vm.upload.files()[0]
      else
        length + ' files'

  vm.onUpload = ->
    if !(vm.upload.width() > 0 and vm.upload.height() > 0)
      vm.errorText("Please insert correct width and height parameters.")
    else if !formData
      vm.errorText('Please upload files first.')
    else
      formData.submit()
      $('#progress .bar').css('width', 0)
      $('#progress').show()

  vm.onCancel = ->
    vm.errorText('')
    vm.paths.removeAll()
    ko.mapping.fromJS(defaultData, {}, vm.upload)
    formData == null

  ko.applyBindings {vm}