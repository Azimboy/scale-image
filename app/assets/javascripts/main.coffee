$ ->
  defaultData =
    width: 100
    height: 100
    files: []

  vm = ko.mapping.fromJS
    upload: defaultData
    errorText: ''

  formData = null
  $uploadForm = $('#upload-form')
  $uploadForm.fileupload
    dataType: 'text'
    autoUpload: no
    replaceFileInput: yes
    singleFileUploads: no
    multipart: yes
    add: (e, data) ->
      formData = data
    fail: (e, data) ->
      console.log(data.jqXHR)
    done: (e, data) ->
      result = data.result
      if result is 'OK'
        alert(result)

  vm.onFilesSelected = (_, event) ->
    vm.errorText('')
    vm.upload.files.removeAll()
    for file in event.target.files
      vm.upload.files.push(file.name)

  vm.filesInfo = ko.computed ->
    length = vm.upload.files().length
    switch length
      when 0 then 'No file chosen'
      when 1 then vm.upload.files()[0]
      else
        length + ' files'

  vm.onUpload = ->
    if vm.upload.files().length > 0
      if formData
        formData.submit()
      else
        $uploadForm.fileupload('send', {files: ''})
      yes
    else
      vm.errorText('Please upload files first.')
      no

  vm.onCancel = ->
    vm.errorText('')
    ko.mapping.fromJS(defaultData, {}, vm.upload)
    formData == null

  ko.applyBindings {vm}