import datetime
import os

from OpenSSL import crypto, SSL
from cryptography import x509
from cryptography.hazmat._oid import NameOID
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric.rsa import RSAPrivateKey
from cryptography.hazmat.primitives.serialization import load_pem_private_key
from cryptography.x509 import Certificate, load_pem_x509_certificate

def _generate_client_cert(
        common_name,
        organization_name,
        organization_unit,
        state_name,
        country_name,
        valid_days,
        ca_key: RSAPrivateKey,
        ca_cert: Certificate,
):
    client_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048,
        backend=default_backend()
    )

    client_subject = x509.Name([
        x509.NameAttribute(x509.NameOID.COMMON_NAME, u"{}".format(common_name)),
        x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u"{}".format(organization_name)),
        x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, u"{}".format(organization_unit)),
        x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u"{}".format(country_name)),
        x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u"{}".format(state_name)),
    ])


    client_cert = (
        x509.CertificateBuilder()
        .subject_name(client_subject)
        .issuer_name(x509.Name([ca_cert.subject.get_attributes_for_oid(NameOID.COMMON_NAME)[0]]))
        .public_key(client_key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.datetime.now(datetime.timezone.utc))
        .not_valid_after(datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=valid_days))
        .sign(
            ca_key,
            hashes.SHA256(),
            default_backend()
        )
    )

    return client_key, client_cert

def _get_ca():
    path = os.path.dirname(os.path.realpath(__file__))
    path = os.path.join(path, "..", "..", "ca")
    with open(os.path.join(path, "ca.crt"), "rb") as f:
        pem_lines = f.read()
    ca_cert = load_pem_x509_certificate(pem_lines, default_backend())

    with open(os.path.join(path, "ca.key"), "rb") as f:
        pem_lines = f.read()

    # todo: the password shouldn't be in code
    ca_key = load_pem_private_key(pem_lines, b"beachist", default_backend())

    return ca_cert, ca_key

def generate_client_cert(
        station_id
):
    ca_cert, ca_key = _get_ca()

    client_key, client_cert = _generate_client_cert(
        station_id,
        "DLRG Ortsgruppe Haffkrug-Scharbeutz e.V.",
        "",
        "SH",
        "DE",
        365,
        ca_key,
        ca_cert,
    )

    encoded_client_private_key = client_key.private_bytes(
        serialization.Encoding.PEM,
        serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption())
    encoded_client_public_key = client_key.public_key().public_bytes(serialization.Encoding.PEM, serialization.PublicFormat.PKCS1)
    encoded_client_cert = client_cert.public_bytes(serialization.Encoding.PEM)
    cert_id = client_cert.serial_number

    # todo: maybe we should just return a dict here instead of a tuple?
    return encoded_client_private_key, encoded_client_public_key, encoded_client_cert, cert_id

def _generate_ca():
    # Create a new RSA key pair for the CA
    ca_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048,
        backend=default_backend()
    )

    # Create a new subject for the CA
    ca_subject = x509.Name([
        x509.NameAttribute(x509.NameOID.COMMON_NAME, u"DLRG Ortsgruppe Haffkrug-Scharbeutz e.V."),
        x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u"DLRG Ortsgruppe Haffkrug-Scharbeutz e.V."),
        x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, u""),
        x509.NameAttribute(x509.NameOID.STREET_ADDRESS, u""),
        x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u"DE"),
        x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u"SH"),
    ])

    # Create a new certificate for the CA
    ca_cert = (
        x509.CertificateBuilder()
        .subject_name(ca_subject)
        .issuer_name(ca_subject)
        .public_key(ca_key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.datetime.now(datetime.timezone.utc))
        .not_valid_after(datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=3650))
        .add_extension(
            x509.BasicConstraints(
                ca=True, path_length=None
            ),
            critical=True
        )
        .sign(
            ca_key,
            hashes.SHA256(),
            default_backend()
        )
    )

    return ca_key, ca_cert

def generate_and_write_ca():
    ca_key, ca_cert = _generate_ca()

    path = os.path.dirname(os.path.realpath(__file__))
    path = os.path.join(path, "..", "..", "ca")
    with open(os.path.join(path, "ca.crt"), "wb") as f:
        f.write(ca_cert.public_bytes(serialization.Encoding.PEM))

    with open(os.path.join(path, "ca.key"), "wb") as f:
        f.write(ca_key.private_bytes(serialization.Encoding.PEM, serialization.PrivateFormat.PKCS8, encryption_algorithm=serialization.BestAvailableEncryption(b"beachist")))
