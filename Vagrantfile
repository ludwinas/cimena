# -*- mode: ruby -*-
Vagrant.configure(2) do |config|
    # which box should we start from
    config.vm.box = "ubuntu/trusty64"
    # port forwards
    config.vm.network "forwarded_port", guest: 5432, host: 5432
    # ansible configuration
    config.vm.provision "ansible" do |ansible|
        ansible.playbook = "ansible/playbook.yml"
        ansible.verbose = "vv"
    end
    # configure virtualbox to have more ram and 2 cpus
    config.vm.provider "virtualbox" do |v|
        v.memory = 2048
        v.cpus = 4
    end

    config.vm.network "private_network", ip: "10.0.0.100"
end
